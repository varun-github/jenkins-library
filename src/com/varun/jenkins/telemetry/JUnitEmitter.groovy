package com.varun.jenkins.telemetry


import hudson.tasks.test.AbstractTestResultAction

public class JUnitEmitter implements  Serializable{
    def pipeline

    def JUnitEmitter(pipeline){
        this.pipeline = pipeline
    }

    def emitTestStatus(){
        def action = this.pipeline.build.getAction(AbstractTestResultAction.class)
        return action.getDisplayName()
    }
}
