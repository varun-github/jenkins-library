package com.varun.jenkins.telemetry


import hudson.tasks.test.AbstractTestResultAction

public class JUnitEmitter implements  Serializable{
    def pipeline

    def JUnitEmitter(pipeline){
        this.pipeline = pipeline
    }

    def emitTestStatus(){
        def hudson.tasks.test.AbstractTestResultAction action = this.pipeline.currentBuild.getRawBuild().getAction(AbstractTestResultAction.class)
        return action.getResult().toString()
    }
}
