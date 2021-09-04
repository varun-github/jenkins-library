package com.varun.jenkins

class Utils implements Serializable{
    def pipeline

    Utils(pipeline){
        this.pipeline = pipeline
    }

    def withMaven(config) {
        if (!config.withArgFile)  {
            print("mandatory argument withArgFile not supplied, doing nothing")
            return
        }
        // def txt = readFile(config.withArgFile)
        // def jsonSlurper = new JsonSlurper()

        def mvnConfig = readJSON file: config.withArgFile
        // print("pipeline config is ${this.pipeline}")
        pipeline.print("mvn config is ${mvnConfig}")
        this.pipeline.print("env config is ${pipeline.env.GIT_URL}")
        // bat("${mvnConfig.MAVEN_HOME}/bin/mvn -s ${mvnConfig.MAVEN_SETTINGS} -DskipTests ${config.do}")


    }
}