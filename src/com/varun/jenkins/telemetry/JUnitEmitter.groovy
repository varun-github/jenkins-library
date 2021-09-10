package com.varun.jenkins.telemetry


import hudson.tasks.test.AbstractTestResultAction
import hudson.tasks.junit.TestResultSummary

public class JUnitEmitter implements  Serializable{
    def pipeline

    def JUnitEmitter(pipeline){
        this.pipeline = pipeline
    }

    def recordTestsAndEmit(){
        def TestResultSummary summary = this.pipeline.junit("target/surefire-reports/TEST*.xml")
        this.pipeline.echo("test summary. passCount =" +summary.passCount+ ", failCount="+ summary.failCount+ ", totalCount=" +summary.totalCount+ ", skipCount=" +summary.skipCount)
//        getResult below returns a hudson.tasks.junit.TestResult, not just a summary.

//        def hudson.tasks.test.AbstractTestResultAction action = this.pipeline.currentBuild.getRawBuild().getAction(AbstractTestResultAction.class)
//        def result = action.getResult()
    }
}
