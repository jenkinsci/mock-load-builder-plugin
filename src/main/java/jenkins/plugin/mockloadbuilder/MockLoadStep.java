package jenkins.plugin.mockloadbuilder;

import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class MockLoadStep extends Step {

    private final long averageDuration;

    @DataBoundConstructor
    public MockLoadStep(long averageDuration) {
        this.averageDuration = averageDuration;
    }

    public long getAverageDuration() {
        return averageDuration;
    }

    @Override public StepExecution start(StepContext context) {
        return new Execution(this, context);
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(FilePath.class, Launcher.class, TaskListener.class);
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
    public static class Execution extends SynchronousNonBlockingStepExecution<Void> {
        private static final long serialVersionUID = 1L;

        private transient final MockLoadStep step;

        Execution(MockLoadStep step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected Void run() throws Exception {
            FilePath ws = getContext().get(FilePath.class);
            Launcher launcher = getContext().get(Launcher.class);
            TaskListener listener = getContext().get(TaskListener.class);
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
