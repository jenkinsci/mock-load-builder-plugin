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

import hudson.Extension;
import hudson.FilePath;
import hudson.slaves.WorkspaceList;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.EnvironmentExpander;
import org.jenkinsci.plugins.workflow.steps.GeneralNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public final class WithMockLoadStep extends Step {

    @DataBoundSetter
    public long averageDuration = 60;

    @DataBoundConstructor
    public WithMockLoadStep() {}

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(context, averageDuration);
    }

    private static final class Execution extends GeneralNonBlockingStepExecution {

        private final long averageDuration;
        private String classpathDirPath;

        Execution(StepContext context, long averageDuration) {
            super(context);
            this.averageDuration = averageDuration;
        }

        @Override
        public boolean start() throws Exception {
            run(() -> {
                var tempDir = WorkspaceList.tempDir(getContext().get(FilePath.class));
                if (tempDir == null) {
                    throw new IOException("could not create temp dir");
                }
                tempDir.mkdirs();
                var classpathDir = tempDir.createTempDir("mock-cp", null);
                classpathDirPath = classpathDir.getRemote();
                classpathDir
                        .child("mock/MockLoad.class")
                        .copyFrom(WithMockLoadStep.class.getResource("/mock/MockLoad.class"));
                var command = "java -classpath \"" + classpathDirPath + "\" mock.MockLoad " + averageDuration;
                getContext()
                        .newBodyInvoker()
                        .withContexts(EnvironmentExpander.merge(
                                getContext().get(EnvironmentExpander.class),
                                EnvironmentExpander.constant(Map.of("MOCK_LOAD_COMMAND", command))))
                        .withCallback(new CleanUp())
                        .start();
            });
            return false;
        }

        private class CleanUp extends BodyExecutionCallback.TailCall {
            @Override
            protected void finished(StepContext context) throws Exception {
                run(() -> {
                    context.get(FilePath.class).child(classpathDirPath).deleteRecursive();
                });
            }
        }
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of(FilePath.class);
        }

        @Override
        public String getFunctionName() {
            return "withMockLoad";
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Mock load with separate sh command";
        }
    }
}
