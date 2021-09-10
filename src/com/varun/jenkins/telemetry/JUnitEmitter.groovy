package com.varun.jenkins.telemetry

import hudson.model.Run
import hudson.tasks.test.AbstractTestResultAction

public class JUnitEmitter implements  Serializable{
    def JUnitEmitter(){}

    def emitTestStatus(Run<?, ?> build){
        def action = build.getAction(AbstractTestResultAction.class)
        return action.getDisplayName()
    }
}
