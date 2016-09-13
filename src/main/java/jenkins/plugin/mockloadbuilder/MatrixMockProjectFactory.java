package jenkins.plugin.mockloadbuilder;

import hudson.Extension;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Fingerprinter;
import hudson.tasks.LogRotator;
import hudson.tasks.junit.JUnitResultArchiver;
import java.io.IOException;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;

@Extension
public class MatrixMockProjectFactory extends MockProjectFactory {
    @Override
    public int getFrequency() {
        return 5;
    }

    @Override
    public int getMultiplier() {
        return 4;
    }

    @Override
    public Job create(ModifiableTopLevelItemGroup ig, String name, Long averageDuration, boolean fastRotate)
            throws IOException {
        MatrixProject project = (MatrixProject) ig.createProject(
                Jenkins.getActiveInstance().getDescriptorByType(MatrixProject.DescriptorImpl.class), name, true);
        project.setBuildDiscarder(fastRotate ? new LogRotator(-1, 5, -1, 1) : new LogRotator(30, 100, 10, 33));
        project.setAxes(new AxisList(new TextAxis("X", "1", "2"), new TextAxis("Y", "1", "2")));
        project.getBuildersList()
                .add(new MockLoadBuilder(averageDuration == null || averageDuration < 0 ? 60L : averageDuration));
        project.getPublishersList().add(new ArtifactArchiver("mock-artifact-*.txt", "", false));
        project.getPublishersList().add(new Fingerprinter("mock-artifact-*.txt", true));
        project.getPublishersList().add(new JUnitResultArchiver("mock-junit.xml", false, null));
        project.setAssignedLabel(null);
        project.save();
        return project;
    }
}
