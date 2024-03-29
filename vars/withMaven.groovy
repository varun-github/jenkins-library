// import com.varun.jenkins.maven.MavenBuilder
// import groovy.json.JsonSlurper

def call(pipeline, config = {}){
    if (!config.withArgFile)  {
        print("mandatory argument withArgFile not supplied, doing nothing")
        return
    }
    // def txt = readFile(config.withArgFile)
    // def jsonSlurper = new JsonSlurper()

    def mvnConfig = readJSON file: config.withArgFile
    print("pipeline config is ${pipeline}")
    print("env config is ${pipeline.env.HOME}")
    // bat("${mvnConfig.MAVEN_HOME}/bin/mvn -s ${mvnConfig.MAVEN_SETTINGS} -DskipTests ${config.do}")

}

// functions to mimic jenkins behavior.

// def readFile(filePath){
//     return new File(filePath).text
// }
// def bat(command){
//     print(command)
// }
// call do: "package", withArgFile: ".jenkins/stage.build.env.json"