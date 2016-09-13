package jenkins.plugin.mockloadbuilder;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.cli.CLICommand;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Fingerprinter;
import hudson.tasks.LogRotator;
import hudson.tasks.junit.JUnitResultArchiver;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

@Extension(optional = true)
public class CreateMockLoadPipeline extends CLICommand {
    @Option(name = "--fast-rotate", usage = "Enable fast rotation of builds")
    public boolean fastRotate;

    @Override
    public String getShortDescription() {
        return "Creates a pipeline that generates a mock load";
    }

    @Argument(index = 0, metaVar = "NAME", usage = "Name of the job to create", required = true)
    public String name;

    @Argument(index = 1, metaVar = "DURATION", usage = "Average Build Duration", required = false)
    public Long averageDuration;

    protected int run() throws Exception {
        Jenkins jenkins = Jenkins.getActiveInstance();
        jenkins.checkPermission(Item.CREATE);

        PipelineMockProjectFactory factory = ExtensionList.lookup(MockProjectFactory.class)
                .get(PipelineMockProjectFactory.class);
        if (factory == null) {
            stderr.println("Pipeline plugin is not installed");
            return -1;
        }
        if (jenkins.getItemByFullName(name) != null) {
            stderr.println("Job '" + name + "' already exists");
            return -1;
        }

        ModifiableTopLevelItemGroup ig = jenkins;
        int i = name.lastIndexOf('/');
        if (i > 0) {
            String group = name.substring(0, i);
            Item item = jenkins.getItemByFullName(group);
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
        factory.create(ig, name, averageDuration, fastRotate);
        return 0;
    }
}
