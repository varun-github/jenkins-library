package com.varun.jenkins

class Utils implements Serializable{
    def pipeline
    // def 

    Utils(pipeline){
        this.pipeline = pipeline
    }

    def withMaven(config) {
        if (!config.withArgFile)  {
            this.pipeline.error("mandatory argument withArgFile not supplied, doing nothing")
            return
        }
        // def txt = readFile(config.withArgFile)
        // def jsonSlurper = new JsonSlurper()
        def mvnConfig = pipeline.readJSON file: config.withArgFile
        this.pipeline.print("maven env config is ${mvnConfig}")
        pipeline.bat("${mvnConfig.MAVEN_HOME}/bin/mvn -s ${mvnConfig.MAVEN_SETTINGS} -DskipTests ${config.do}")
    }
    def withDocker(config){
        if (!config.withArgFile)  {
            this.pipeline.error("mandatory argument withArgFile not supplied, doing nothing")
            return
        }
        def dockerConfig = pipeline.readJSON file: config.withArgFile

        // hack to prevent making sensitive information public. to be removed with private git repos
        dockerConfig.AWS_REGION = this.pipeline.env.AWS_REGION
        dockerConfig.AWS_PROFILE = this.pipeline.env.AWS_PROFILE
        dockerConfig.ECR_REGISTRY = this.pipeline.env.ECR_REGISTRY
        dockerConfig.DOCKER_REPO_PREFIX = this.pipeline.env.DOCKER_REPO_PREFIX



        this.pipeline.print("docker env config is ${dockerConfig}")
        // TODO: handle the npe possiblity below
        def git_repo = this.pipeline.env.GIT_URL.replaceFirst(/.*\/([\w-]+).*/, '$1')
        this.pipeline.bat("aws --profile ${dockerConfig.AWS_PROFILE} --region ${dockerConfig.AWS_REGION} ecr get-login-password  | docker login --username AWS --password-stdin ${dockerConfig.ECR_REGISTRY}")
        def img = this.pipeline.docker.build("${dockerConfig.ECR_REGISTRY}/${dockerConfig.DOCKER_REPO_PREFIX}/${git_repo}:${this.pipeline.env.BRANCH_NAME}.${this.pipeline.env.BUILD_ID}")
        img.push()
    }
    def withTerraform(config){
        if (!config.withArgFile)  {
            this.pipeline.error("mandatory argument withArgFile not supplied, doing nothing")
            return
        }
        def tfVars = pipeline.readJSON file: config.withArgFile

        // hack to prevent making sensitive information public. to be removed with private git repos
        tfVars.aws_region = this.pipeline.env.AWS_REGION
        tfVars.cluster_name = this.pipeline.env.cluster_name
        tfVars.image_name = this.pipeline.env.image_name
        tfVars.vpc_id = this.pipeline.env.vpc_id
        tfVars.lb_listener_arn = this.pipeline.env.lb_listener_arn
        tfVars.tags = pipeline.readJSON text: this.pipeline.env.tags
        this.pipeline.print("TF vars = ${tfVars}")

        def ecsSvcPayload = this.pipeline.libraryResource ('com/varun/terraform/fargate-service/ecs_service.tf')
        ecsSvcPayload = ecsSvcPayload.replaceAll("bucket = \"\"", "bucket = \"${this.pipeline.env.TF_STATE_BUCKET}\"")
        ecsSvcPayload = ecsSvcPayload.replaceAll("region = \"\"", "region = \"${this.pipeline.env.AWS_REGION}\"")
        ecsSvcPayload = ecsSvcPayload.replaceAll("profile = \"\"", "profile = \"${this.pipeline.env.AWS_PROFILE}\"")
        this.pipeline.print("TF svc conte = ${ecsSvcPayload}")

        // write tf files to workspace
        this.pipeline.writeJSON file: 'tfVars.json', json: tfVars
        this.pipeline.writeFile file: 'ecs_service.tf', text: ecsSvcPayload
        this.pipeline.writeFile file: 'variables.tf', text: this.pipeline.libraryResource('com/varun/terraform/fargate-service/variables.tf')

        //run tf
        this.pipeline.print("WORKSPACE = ${this.pipeline.env.WORKSPACE}")
        this.pipeline.docker.image('hashicorp/terraform:1.0.5').run("--rm -v ${this.pipeline.env.WORKSPACE}:/workspace -v c:/Users/write/.aws:/root/.aws -w /workspace", "init") 
    }
}