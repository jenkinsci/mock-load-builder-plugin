package jenkins.plugin.mockloadbuilder;

import hudson.Extension;
import hudson.model.Job;
import hudson.tasks.LogRotator;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.io.IOException;

@Extension(optional = true)
public class PipelineMockProjectFactory extends MockProjectFactory {
    @Override
    public int getFrequency() {
        return Config.get().getPipelineFrequency();
    }

    @Override
    public Job create(ModifiableTopLevelItemGroup ig, String name, Long averageDuration, boolean fastRotate) throws IOException {
        WorkflowJob project = (WorkflowJob) ig.createProject(
                Jenkins.get().getDescriptorByType(WorkflowJob.DescriptorImpl.class), name, true);
        project.setBuildDiscarder(createBuildDiscarder(fastRotate));
        project.setDefinition(new CpsFlowDefinition(String.format(
                "node {%n"
                        + "  mockLoad %d%n"
                        + "  archiveArtifacts allowEmptyArchive: true, artifacts: 'mock-artifact-*.txt'%n"
                        + "  fingerprint 'mock-artifact-*.txt'%n"
                        + "  junit 'mock-junit.xml'%n"
                        + "}",
                averageDuration == null || averageDuration < 0 ? Long.valueOf(60L) : averageDuration
        ), true));
        project.save();
        return project;
    }

    @Override
    public String getName() {
        return "Pipeline";
    }
}
