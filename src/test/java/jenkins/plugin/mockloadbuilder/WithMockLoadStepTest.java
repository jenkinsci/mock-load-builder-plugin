/*
 * The MIT License
 *
 * Copyright 2023 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jenkins.plugin.mockloadbuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import hudson.Functions;
import hudson.model.Result;
import java.util.Set;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.jvnet.hudson.test.junit.jupiter.BuildWatcherExtension;
import org.jvnet.hudson.test.junit.jupiter.JenkinsSessionExtension;

class WithMockLoadStepTest {

    @SuppressWarnings("unused")
    @RegisterExtension
    private static final BuildWatcherExtension BUILD_WATCHER = new BuildWatcherExtension();

    @RegisterExtension
    private final JenkinsSessionExtension extension = new JenkinsSessionExtension();

    @Test
    void smokes() throws Throwable {
        assumeFalse(Functions.isWindows(), "TODO build resumption not working on Windows; problem with dummy agent?");
        extension.then(r -> {
            r.createSlave("remote", null, null);
            var p = r.createProject(WorkflowJob.class, "p");
            p.setDefinition(new CpsFlowDefinition("""
                            node('remote') {
                              withMockLoad(averageDuration: 15, testFailureIgnore: true) {
                                if (isUnix()) {
                                  sh MOCK_LOAD_COMMAND
                                } else {
                                  bat MOCK_LOAD_COMMAND
                                }
                              }
                              junit 'mock-junit.xml'
                              archiveArtifacts artifacts: 'mock-artifact-*.txt', fingerprint: true
                            }""", true));
            var b = p.scheduleBuild2(0).waitForStart();
            r.waitForMessage("[INFO] Building mock-load 1.0-SNAPSHOT", b);
        });
        extension.then(r -> {
            var b = r.jenkins.getItemByFullName("p", WorkflowJob.class).getBuildByNumber(1);
            r.waitForCompletion(b);
            assertThat(b.getResult(), is(in(Set.of(Result.SUCCESS, Result.UNSTABLE))));
            r.assertLogContains("[INFO] Reactor Summary:", b);
        });
    }
}
