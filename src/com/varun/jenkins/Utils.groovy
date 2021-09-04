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
        def tfConfig = pipeline.readJSON file: config.withArgFile

        // hack to prevent making sensitive information public. to be removed with private git repos
        tfConfig.aws_region = this.pipeline.env.AWS_REGION
        tfConfig.cluster_name = this.pipeline.env.cluster_name
        tfConfig.image_name = this.pipeline.env.image_name
        tfConfig.vpc_id = this.pipeline.env.vpc_id
        tfConfig.lb_listener_arn = this.pipeline.env.lb_listener_arn
        tfConfig.tags = pipeline.readJSON text: this.pipeline.env.tags

        
        tfConfig.AWS_PROFILE = this.pipeline.env.AWS_PROFILE
        tfConfig.ECR_REGISTRY = this.pipeline.env.ECR_REGISTRY
        tfConfig.DOCKER_REPO_PREFIX = this.pipeline.env.DOCKER_REPO_PREFIX

        this.pipeline.print("TF config = ${tfConfig}")

    }
}