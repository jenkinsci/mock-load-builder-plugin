package jenkins.plugin.mockloadbuilder;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.InputStream;
import java.io.OutputStream;

public class MockLoadStep extends AbstractStepImpl {

    private final long averageDuration;

    @DataBoundConstructor
    public MockLoadStep(long averageDuration) {
        this.averageDuration = averageDuration;
    }

    public long getAverageDuration() {
        return averageDuration;
    }


    @Extension(optional = true)
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        public DescriptorImpl() {
            super(Execution.class);
        }

        @Override
        public String getFunctionName() {
            return "mockLoad";
        }

        @Override
        public String getDisplayName() {
            return Messages.MockLoadBuilder_DisplayName();
        }
    }

    /**
     * @author Stephen Connolly
     */
    public static class Execution extends AbstractSynchronousNonBlockingStepExecution<Void> {
        /**
         * Standardize serialization.
         */
        private static final long serialVersionUID = 1L;
        /**
         * The listener.
         */
        @StepContextParameter
        private transient TaskListener listener;
        /**
         * The location to perform the mock build.
         */
        @StepContextParameter
        private transient FilePath ws;
        /**
         * The launcher to use to perform the mock build
         */
        @StepContextParameter
        private transient Launcher launcher;
        /**
         * The step configuration.
         */
        @Inject
        private transient MockLoadStep step;

        @Override
        protected Void run() throws Exception {
            if (MockProjectFactory.mode) {
                ws.act(ws.asCallableWith(new MockLoadBuilder.NoFork(step.getAverageDuration(), listener)));
            } else {
                try (InputStream is = getClass().getResourceAsStream("/mock/MockLoad.class");
                     OutputStream os = ws.child("mock/MockLoad.class").write()) {
                    IOUtils.copy(is, os);
                }
                launcher.launch().pwd(ws).cmds("java", "mock.MockLoad", Long.toString(step.getAverageDuration()))
                        .stdout(listener)
                        .start()
                        .join();
            }
            return null;
        }
    }
}
