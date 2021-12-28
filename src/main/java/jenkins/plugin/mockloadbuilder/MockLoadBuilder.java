package jenkins.plugin.mockloadbuilder;

import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import java.io.File;
import jenkins.MasterToSlaveFileCallable;
import jenkins.tasks.SimpleBuildStep;
import mock.MockLoad;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MockLoadBuilder extends Builder implements SimpleBuildStep {

    private final long averageDuration;

    @DataBoundConstructor
    public MockLoadBuilder(long averageDuration) {
        this.averageDuration = averageDuration;
    }

    public long getAverageDuration() {
        return averageDuration;
    }

    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull Launcher launcher, @NonNull TaskListener listener) throws InterruptedException, IOException {
        if (MockProjectFactory.mode) {
            workspace.act(workspace.asCallableWith(new NoFork(averageDuration, listener)));
        } else {
            try (InputStream is = getClass().getResourceAsStream("/mock/MockLoad.class");
                 OutputStream os = workspace.child("mock/MockLoad.class").write()) {
                IOUtils.copy(is, os);
            }
            launcher.launch().pwd(workspace).cmds("java", "mock.MockLoad", Long.toString(averageDuration))
                    .stdout(listener)
                    .start()
                    .join();
        }
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
