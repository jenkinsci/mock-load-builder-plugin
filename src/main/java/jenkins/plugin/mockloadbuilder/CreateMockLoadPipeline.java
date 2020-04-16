package jenkins.plugin.mockloadbuilder;

import hudson.Extension;

@Extension(optional = true)
public class CreateMockLoadPipeline extends AbstractCreateMockLoad {
    @Override
    public String getShortDescription() {
        return "Creates a pipeline that generates a mock load";
    }

    @Override
    protected Class<? extends MockProjectFactory> getProjectFactoryClass() {
        return PipelineMockProjectFactory.class;
    }
}
