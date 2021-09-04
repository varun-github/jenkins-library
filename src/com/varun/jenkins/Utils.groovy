package com.varun.jenkins

class Utils implements Serializable{
    def pipeline

    Utils(pipeline){
        this.pipeline = pipeline
    }

    def withMaven(config) {
        if (!config.withArgFile)  {
            this.pipeline.print("mandatory argument withArgFile not supplied, doing nothing")
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
            this.pipeline.print("mandatory argument withArgFile not supplied, doing nothing")
            return
        }
        def dockerConfig = pipeline.readJSON file: config.withArgFile
        this.pipeline.print("docker env config is ${dockerConfig}")
        // TODO: handle the npe possiblity below
        def git_repo = this.pipeline.env.GIT_URL.replaceFirst(/.*\/([\w-]+).*/, '$1')
        this.pipeline.bat("aws --profile ${dockerConfig.AWS_PROFILE} --region ${dockerConfig.AWS_REGION} ecr get-login-password  | docker login --username AWS --password-stdin ${dockerConfig.ECR_REGISTRY}")
        def img = this.pipeline.docker.build("${dockerConfig.ECR_REGISTRY}/${dockerConfig.DOCKER_REPO_PREFIX}/${git_repo}:${this.pipeline.env.BRANCH_NAME}.${this.pipeline.env.BUILD_ID}")
        img.push()
    }
}