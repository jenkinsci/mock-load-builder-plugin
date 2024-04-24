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
import static org.junit.Assume.assumeFalse;

import hudson.Functions;
import hudson.model.Result;
import java.util.Set;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsSessionRule;

public class WithMockLoadStepTest {

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule
    public JenkinsSessionRule rr = new JenkinsSessionRule();

    @Test
    public void smokes() throws Throwable {
        assumeFalse("TODO build resumption not working on Windows; problem with dummy agent?", Functions.isWindows());
        rr.then(r -> {
            r.createSlave("remote", null, null);
            var p = r.createProject(WorkflowJob.class, "p");
            p.setDefinition(new CpsFlowDefinition(
                    "node('remote') {\n" + "  withMockLoad(averageDuration: 15, testFailureIgnore: true) {\n"
                            + "    if (isUnix()) {\n"
                            + "      sh MOCK_LOAD_COMMAND\n"
                            + "    } else {\n"
                            + "      bat MOCK_LOAD_COMMAND\n"
                            + "    }\n"
                            + "  }\n"
                            + "  junit 'mock-junit.xml'\n"
                            + "  archiveArtifacts artifacts: 'mock-artifact-*.txt', fingerprint: true\n"
                            + "}",
                    true));
            var b = p.scheduleBuild2(0).waitForStart();
            r.waitForMessage("[INFO] Building mock-load 1.0-SNAPSHOT", b);
        });
        rr.then(r -> {
            var b = r.jenkins.getItemByFullName("p", WorkflowJob.class).getBuildByNumber(1);
            r.waitForCompletion(b);
            assertThat(b.getResult(), is(in(Set.of(Result.SUCCESS, Result.UNSTABLE))));
            r.assertLogContains("[INFO] Reactor Summary:", b);
        });
    }
}
