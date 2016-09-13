package jenkins.plugin.mockloadbuilder;

import com.google.inject.Inject;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

/**
 * @author Stephen Connolly
 */
public class MockLoadExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {
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
    @Inject(optional = true)
    private transient MockLoadStep step;

    @Override
    protected Void run() throws Exception {
        if (MockProjectFactory.mode) {
            assert ws != null;

            launcher.getChannel().call(ws.asCallableWith(new MockLoadBuilder.NoFork(step.getAverageDuration(), listener)));
        } else {
            InputStream is = getClass().getResourceAsStream("/mock/MockLoad.class");
            assert ws != null;
            try {
                OutputStream os = ws.child("mock/MockLoad.class").write();
                try {
                    IOUtils.copy(is, os);
                } finally {
                    IOUtils.closeQuietly(os);
                }
            } finally {
                IOUtils.closeQuietly(is);
            }
            launcher.launch().pwd(ws).cmds("java", "mock.MockLoad", Long.toString(step.getAverageDuration()))
                    .stdout(listener)
                    .start()
                    .join();
        }
        return null;
    }
}
