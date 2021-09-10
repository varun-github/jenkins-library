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
        def TestResultSummary summary = this.pipeline.junit("target/surefire-reports/TEST*.xml")
        if (summary == null) {
            this.pipeline.echo("No test seems to have been recorded. No results to be emitted.")
            return
        }
        this.pipeline.echo("test summary to be emitted. passCount =" +summary.passCount+ ", failCount="+ summary.failCount+ ", totalCount=" +summary.totalCount+ ", skipCount=" +summary.skipCount)

        def jsonMessage = [
            detailURL: this.pipeline.env.BUILD_URL + "/testReport/api/json",
            testPassCount: summary.passCount,
            failCount: summary.failCount,
            totalCount: summary.totalCount,
            skipCount: summary.skipCount
        ]
        def jsonMessageAsString = this.pipeline.writeJSON json: jsonMessage, returnText: true
        // call cloud studio webhook for test result callback.
        def csConf = this.pipeline.readJSON text:  this.pipeline.libraryResource ('com/varun/cloud-studio/config.json')
        
        try {
            def csConn = new URL(csConf.cloudStudioBaseURL).openConnection()
            csConn.setRequestMethod("POST")
            csConn.setDoOutput(true)
            csConn.setRequestProperty("Content-Type", "application/json")
            csConn.getOutputStream().write(jsonMessageAsString.getBytes("UTF-8"));
            def postRC = csConn.getResponseCode();
            this.pipeline.echo("CS API Response Code: " +postRC)

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
