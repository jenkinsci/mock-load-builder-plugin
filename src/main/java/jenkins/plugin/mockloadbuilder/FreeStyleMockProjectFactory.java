package jenkins.plugin.mockloadbuilder;

import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import java.io.IOException;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;

@Extension
public class FreeStyleMockProjectFactory extends MockProjectFactory {
    @Override
    public int getFrequency() {
        return Config.get().getFreestyleFrequency();
    }

    @Override
    public Job create(ModifiableTopLevelItemGroup ig, String name, Long averageDuration, boolean fastRotate)
            throws IOException {
        FreeStyleProject project = createProject(ig, name);
        project.setBuildDiscarder(createBuildDiscarder(fastRotate));
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
        return "Freestyle";
    }

    private FreeStyleProject createProject(ModifiableTopLevelItemGroup ig, String name) throws IOException {
        return (FreeStyleProject)
                ig.createProject(Jenkins.get().getDescriptorByType(FreeStyleProject.DescriptorImpl.class), name, true);
    }
}
