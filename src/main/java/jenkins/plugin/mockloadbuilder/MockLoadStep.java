package jenkins.plugin.mockloadbuilder;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;

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
            super(MockLoadExecution.class);
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
}
