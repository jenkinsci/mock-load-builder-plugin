package jenkins.plugin.mockloadbuilder;

import hudson.Extension;
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
public class FreeStyleMockProjectFactory extends MockProjectFactory {
    @Override
    public int getFrequency() {
        return 70;
    }

    @Override
    public Job create(ModifiableTopLevelItemGroup ig, String name, Long averageDuration, boolean fastRotate)
            throws IOException {
        FreeStyleProject project = (FreeStyleProject) ig.createProject(
                Jenkins.getActiveInstance().getDescriptorByType(FreeStyleProject.DescriptorImpl.class), name, true);
        project.setBuildDiscarder(fastRotate ? new LogRotator(-1, 5, -1, 1) : new LogRotator(30, 100, 10, 33));
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
