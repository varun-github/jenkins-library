package com.varun.jenkins.telemetry

import hudson.tasks.junit.CaseResult
import hudson.tasks.test.AbstractTestResultAction
import hudson.tasks.junit.TestResultSummary
import hudson.tasks.junit.TestResult

public class JUnitEmitter implements  Serializable{
    def pipeline

    def JUnitEmitter(pipeline){
        this.pipeline = pipeline
    }

    def recordTestsAndEmit(){
        def jsonMessage = [
            pipelineDetailURL: this.pipeline.env.BUILD_URL + "wfapi/describe",
            jiraEpicKey: this.pipeline.env.EPIC_KEY,
            // jiraStoryKey: this.pipeline.env.STORY_KEY
        ]

        def TestResultSummary summary = this.pipeline.junit("target/surefire-reports/TEST*.xml")
        if (summary == null) {
            this.pipeline.echo("No test seems to have been recorded. No results to be emitted.")
        } else {
            this.pipeline.echo("test summary to be emitted. passCount =" +summary.passCount+ ", failCount="+ summary.failCount+ ", totalCount=" +summary.totalCount+ ", skipCount=" +summary.skipCount)
            jsonMessage.putAll([
                testDetailURL: this.pipeline.env.BUILD_URL + "testReport/api/json",
                testPassCount: summary.passCount,
                failCount: summary.failCount,
                totalCount: summary.totalCount,
                skipCount: summary.skipCount,
            ])
        }

        def jsonMessageAsString = this.pipeline.writeJSON json: jsonMessage, returnText: true
        // call cloud studio webhook for test result callback.
        def csConf = this.pipeline.readJSON text:  this.pipeline.libraryResource ('com/varun/cloud-studio/config.json')
        
        try {
            def emitResponse = this.pipeline.httpRequest url: csConf.cloudStudioBaseURL, contentType: 'APPLICATION_JSON', httpMode: 'PUT', requestBody: jsonMessageAsString, timeout: 60
            this.pipeline.echo("HTTP Requested emitted, response code: " +emitResponse.status)
            this.pipeline.echo("HTTP Requested emitted, response payload: " +emitResponse.content)
        } catch (Exception e){
            this.pipeline.echo("Exception encountered while emitting junit results")
            def sw = new java.io.StringWriter()
            def pw = new java.io.PrintWriter(sw)
            e.printStackTrace(pw)
            this.pipeline.echo(sw.toString())
        }

 
//        getResult below returns a hudson.tasks.junit.TestResult, not just a summary.

        /**
         * The following will allow more detailed information about each test case run, but needs whitelisting in jenkins. The errors typically are:
         * org.jenkinsci.plugins.scriptsecurity.sandbox.RejectedAccessException: Scripts not permitted to use method hudson.tasks.test.TestResult getFailedTests
         */
//        def AbstractTestResultAction action = this.pipeline.currentBuild.getRawBuild().getAction(AbstractTestResultAction.class)
//        def TestResult result = action.getResult()
//        for (CaseResult c: result.getFailedTests()){
//            this.pipeline.echo("Failed test: "+ c.durationString+ ", name: " +c.name+ " status:" +c.status)
//        }
    }
}
