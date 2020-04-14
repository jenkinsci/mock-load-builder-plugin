package jenkins.plugin.mockloadbuilder;

import hudson.Extension;

@Extension
public class CreateMockLoadJob extends AbstractCreateMockLoad {
    @Override
    public String getShortDescription() {
        return "Creates a freestyle job that generates a mock load";
    }

    @Override
    protected Class<? extends MockProjectFactory> getProjectFactoryClass() {
        return FreeStyleMockProjectFactory.class;
    }
}
