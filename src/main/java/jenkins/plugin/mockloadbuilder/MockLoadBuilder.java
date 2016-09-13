package jenkins.plugin.mockloadbuilder;

import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import java.io.File;
import jenkins.MasterToSlaveFileCallable;
import mock.MockLoad;
import hudson.Extension;
import hudson.FilePath;
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
        if (MockProjectFactory.mode) {
            FilePath workspace = build.getWorkspace();
            assert workspace != null;
            launcher.getChannel().call(workspace.asCallableWith(new NoFork(averageDuration, listener)));
        } else {
            InputStream is = getClass().getResourceAsStream("/mock/MockLoad.class");
            FilePath workspace = build.getWorkspace();
            assert workspace != null;
            try {
                OutputStream os = workspace.child("mock/MockLoad.class").write();
                try {
                    IOUtils.copy(is, os);
                } finally {
                    IOUtils.closeQuietly(os);
                }
            } finally {
                IOUtils.closeQuietly(is);
            }
            launcher.launch().pwd(workspace).cmds("java", "mock.MockLoad", Long.toString(averageDuration))
                    .stdout(listener)
                    .start()
                    .join();
        }
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

    static class NoFork extends MasterToSlaveFileCallable<Boolean> {
        private final TaskListener listener;
        private long averageDuration;

        NoFork(long averageDuration, TaskListener listener) {
            this.listener = listener;
            this.averageDuration = averageDuration;
        }

        @Override
        public Boolean invoke(File file, VirtualChannel virtualChannel) throws IOException, InterruptedException {
            try {
                return MockLoad.build(file, averageDuration, listener.getLogger());
            } catch (InterruptedException e) {
                throw new IOException("Interrupted", e);
            }
        }
    }
}
