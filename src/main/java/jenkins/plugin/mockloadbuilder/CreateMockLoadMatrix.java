package jenkins.plugin.mockloadbuilder;

import org.jenkinsci.plugins.variant.OptionalExtension;

@OptionalExtension(requirePlugins = "matrix-project")
public class CreateMockLoadMatrix extends AbstractCreateMockLoad {
    @Override
    public String getShortDescription() {
        return "Creates a matrix project that generates a mock load";
    }

    @Override
    protected Class<? extends MockProjectFactory> getProjectFactoryClass() {
        return MatrixMockProjectFactory.class;
    }
}
