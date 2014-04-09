package jenkins.plugin.mockloadbuilder;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Stephen Connolly
 */
public class MockLoadBuilder extends Builder {

    private final long averageDuration;

    @DataBoundConstructor
    public MockLoadBuilder(long averageDuration) {
        this.averageDuration = averageDuration;
    }

    public long getAverageDuration() {
        return averageDuration;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        InputStream is = getClass().getResourceAsStream("/MockLoad.class");
        try {
            OutputStream os = build.getWorkspace().child("MockLoad.class").write();
            try {
                IOUtils.copy(is, os);
            } finally {
                IOUtils.closeQuietly(os);
            }
        } finally {
            IOUtils.closeQuietly(is);
        }
        launcher.launch().pwd(build.getWorkspace()).cmds("java", "MockLoad", Long.toString(averageDuration))
                .stdout(listener)
                .start()
                .join();
        return true;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.MockLoadBuilder_DisplayName();
        }
    }
}
