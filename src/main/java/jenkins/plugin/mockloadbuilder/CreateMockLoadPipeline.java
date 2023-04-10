package jenkins.plugin.mockloadbuilder;

import org.jenkinsci.plugins.variant.OptionalExtension;

@OptionalExtension(
        requirePlugins = {"workflow-job", "workflow-cps", "workflow-basic-steps", "workflow-durable-task-step"})
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
