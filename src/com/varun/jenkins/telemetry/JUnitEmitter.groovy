package com.varun.jenkins.telemetry

import hudson.tasks.junit.CaseResult

hudson.tasks.junit.TestResult
import hudson.tasks.test.AbstractTestResultAction
import hudson.tasks.junit.TestResultSummary

public class JUnitEmitter implements  Serializable{
    def pipeline

    def JUnitEmitter(pipeline){
        this.pipeline = pipeline
    }

    def recordTestsAndEmit(){
        def TestResultSummary summary = this.pipeline.junit("target/surefire-reports/TEST*.xml")
        def hudson.tasks.test.AbstractTestResultAction action = this.pipeline.currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
        if (action == null || summary == null){
            this.pipeline.echo("No test results found to be recorded and emitted.")
            return
        }
        this.pipeline.echo("test summary. passCount =" +summary.passCount+ ", failCount="+ summary.failCount+ ", totalCount=" +summary.totalCount+ ", skipCount=" +summary.skipCount)

//        getResult below returns a hudson.tasks.junit.TestResult, not just a summary.

        def hudson.tasks.junit.TestResult result = action.getResult()
        def passedSummary = new StringBuffer()
        for (CaseResult r : result.getPassedTests()){
            passedSummary.append("\t" +r.name+ ", " +r.duration+ ", "+r.status+ ", " +r.durationString+ "\n")
        }
        this.pipeline.echo("passed results: \n" +passedSummary.toString())
    }
}
