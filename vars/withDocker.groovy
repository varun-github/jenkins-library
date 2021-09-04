// import com.varun.jenkins.maven.MavenBuilder
import groovy.json.JsonSlurper

def call(config = {}){
    if (!config.withArgFile)  {
        print("mandatory argument file not supplied, doing nothing")
        return
    }

    def dockerConfig = readJSON file: config.withArgFile

    bat(dockerConfig)

}

// functions to mimic jenkins behavior.

def readJSON(config){
    def slurper = new JsonSlurper()
    return slurper.parseText(new File(config.file).text)
}
def bat(command){
    print(command)
}
call do: "package", withArgFile: ".jenkins/stage.build.docker.env.json"