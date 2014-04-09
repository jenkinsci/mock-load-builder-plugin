package jenkins.plugin.mockloadbuilder;

import hudson.Extension;
import hudson.cli.CLICommand;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Fingerprinter;
import hudson.tasks.LogRotator;
import hudson.tasks.junit.JUnitResultArchiver;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.args4j.Argument;

import java.util.Random;

/**
 * @author Stephen Connolly
 */
@Extension
public class CreateMockLoadJobs extends CLICommand {
    @Override
    public String getShortDescription() {
        return "Creates a job that generates a mock load";
    }

    @Argument(index = 0, metaVar = "COUNT", usage = "Number of jobs to create", required = true)
    public Integer count;

    @Argument(index = 1, metaVar = "DURATION", usage = "Average build duration, -1 will give a typical random duration to each job", required = false)
    public Long averageDuration;

    @Argument(index = 2, metaVar = "FOLDER", usage = "Where to create the jobs", required = false)
    public String group;

    protected int run() throws Exception {
        Jenkins h = Jenkins.getInstance();
        h.checkPermission(Item.CREATE);

        ModifiableTopLevelItemGroup ig = h;
        if (StringUtils.isNotBlank(group)) {
            Item item = h.getItemByFullName(group);
            if (item == null) {
                throw new IllegalArgumentException("Unknown ItemGroup " + group);
            }

            if (item instanceof ModifiableTopLevelItemGroup) {
                ig = (ModifiableTopLevelItemGroup) item;
            } else {
                throw new IllegalArgumentException("Can't create job from CLI in " + group);
            }
        }


        Random entropy = new Random();
        for (int n = 0; n < count; n++) {
            String name = "mock-load-job-" + StringUtils.leftPad(Integer.toString(n+1), 5, '0');
            if (ig.getItem(name) != null) {
                continue;
            }
            Jenkins.checkGoodName(name);
            FreeStyleProject project =
                    (FreeStyleProject) ig
                            .createProject(Jenkins.getInstance().getDescriptorByType(FreeStyleProject.DescriptorImpl.class),
                                    name,
                                    true);
            project.setBuildDiscarder(new LogRotator(30, 100, 10, 33));
            project.getBuildersList()
                    .add(new MockLoadBuilder(averageDuration == null || averageDuration < 0
                            ? Math.max(20,
                            (long) (180 + (entropy.nextFloat() < 0.5 ? -120 : 360) * Math.abs(entropy.nextGaussian())))
                            : averageDuration));
            project.getPublishersList().add(new ArtifactArchiver("mock-artifact-*.txt", "", false));
            project.getPublishersList().add(new Fingerprinter("", true));
            project.getPublishersList().add(new JUnitResultArchiver("mock-junit.xml", false, null));
            project.setAssignedLabel(null);
            project.save();
        }
        return 0;
    }
}
