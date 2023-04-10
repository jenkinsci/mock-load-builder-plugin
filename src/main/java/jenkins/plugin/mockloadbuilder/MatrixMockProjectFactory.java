package jenkins.plugin.mockloadbuilder;

import hudson.matrix.AxisList;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.Job;
import java.io.IOException;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.jenkinsci.plugins.variant.OptionalExtension;

@OptionalExtension(requirePlugins = "matrix-project")
public class MatrixMockProjectFactory extends MockProjectFactory {
    @Override
    public int getFrequency() {
        return Config.get().getMatrixFrequency();
    }

    @Override
    public int getMultiplier() {
        return Config.get().getMatrixMultiplier();
    }

    @Override
    public Job create(ModifiableTopLevelItemGroup ig, String name, Long averageDuration, boolean fastRotate)
            throws IOException {
        MatrixProject project = createProject(ig, name);
        project.setBuildDiscarder(createBuildDiscarder(fastRotate));
        project.setAxes(createAxisList());
        project.getBuildersList().add(createMockLoadBuilder(averageDuration));
        project.getPublishersList().add(createArtifactArchiver());
        project.getPublishersList().add(createFingerprinter());
        project.getPublishersList().add(createJunitArchiver());
        project.setAssignedLabel(null);
        project.save();
        return project;
    }

    @Override
    public String getName() {
        return "Matrix";
    }

    private MatrixProject createProject(ModifiableTopLevelItemGroup ig, String name) throws IOException {
        return (MatrixProject)
                ig.createProject(Jenkins.get().getDescriptorByType(MatrixProject.DescriptorImpl.class), name, true);
    }

    private AxisList createAxisList() {
        return new AxisList(new TextAxis("X", "1", "2"), new TextAxis("Y", "1", "2"));
    }
}
