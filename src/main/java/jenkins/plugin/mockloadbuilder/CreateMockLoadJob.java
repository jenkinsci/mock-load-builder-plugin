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
import org.kohsuke.args4j.Argument;

/**
 * @author Stephen Connolly
 */
@Extension
public class CreateMockLoadJob extends CLICommand {
    @Override
    public String getShortDescription() {
        return "Creates a job that generates a mock load";
    }

    @Argument(index = 0, metaVar = "NAME", usage = "Name of the job to create", required = true)
    public String name;

    @Argument(index = 1, metaVar = "DURATION", usage = "Average Build Duration", required = false)
    public Long averageDuration;

    protected int run() throws Exception {
        Jenkins h = Jenkins.getInstance();
        h.checkPermission(Item.CREATE);

        if (h.getItemByFullName(name) != null) {
            stderr.println("Job '" + name + "' already exists");
            return -1;
        }

        ModifiableTopLevelItemGroup ig = h;
        int i = name.lastIndexOf('/');
        if (i > 0) {
            String group = name.substring(0, i);
            Item item = h.getItemByFullName(group);
            if (item == null) {
                throw new IllegalArgumentException("Unknown ItemGroup " + group);
            }

            if (item instanceof ModifiableTopLevelItemGroup) {
                ig = (ModifiableTopLevelItemGroup) item;
            } else {
                throw new IllegalArgumentException("Can't create job from CLI in " + group);
            }
            name = name.substring(i + 1);
        }

        Jenkins.checkGoodName(name);
        FreeStyleProject project =
                (FreeStyleProject) ig
                        .createProject(Jenkins.getInstance().getDescriptorByType(FreeStyleProject.DescriptorImpl.class),
                                name,
                                true);
        project.setBuildDiscarder(new LogRotator(30, 100, 10, 33));
        project.getBuildersList()
                .add(new MockLoadBuilder(averageDuration == null || averageDuration < 0 ? 60L : averageDuration));
        project.getPublishersList().add(new ArtifactArchiver("mock-artifact-*.txt", "", false));
        project.getPublishersList().add(new Fingerprinter("", true));
        project.getPublishersList().add(new JUnitResultArchiver("mock-junit.xml", false, null));
        project.save();
        return 0;
    }
}
