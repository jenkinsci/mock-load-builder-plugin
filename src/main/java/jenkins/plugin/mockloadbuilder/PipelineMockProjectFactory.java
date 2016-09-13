package jenkins.plugin.mockloadbuilder;

import hudson.Extension;
import hudson.model.Job;
import hudson.tasks.LogRotator;
import java.io.IOException;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

@Extension(optional = true)
public class PipelineMockProjectFactory extends MockProjectFactory {
    @Override
    public int getFrequency() {
        return 5;
    }

    @Override
    public Job create(ModifiableTopLevelItemGroup ig, String name, Long averageDuration, boolean fastRotate) throws IOException {
        WorkflowJob project = (WorkflowJob) ig.createProject(
                Jenkins.getActiveInstance().getDescriptorByType(WorkflowJob.DescriptorImpl.class), name, true);
        project.setBuildDiscarder(fastRotate ? new LogRotator(-1, 5, -1, 1) : new LogRotator(30, 100, 10, 33));
        project.setDefinition(new CpsFlowDefinition(String.format(
                "node {%n"
                        + "  mockLoad %d%n"
                        + "  archive 'mock-artifact-*.txt'%n"
                        + "  step([$class: 'Fingerprinter', testResults: 'mock-artifact-*.txt'])%n"
                        + "  step([$class: 'JUnitResultArchiver', testResults: 'mock-junit.xml'])%n"
                        + "}",
                averageDuration == null || averageDuration < 0 ? Long.valueOf(60L) : averageDuration
        )));
        project.save();
        return project;
    }
}
