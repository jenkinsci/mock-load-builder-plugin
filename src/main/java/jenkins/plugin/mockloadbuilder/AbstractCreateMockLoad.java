package jenkins.plugin.mockloadbuilder;

import hudson.ExtensionList;
import hudson.cli.CLICommand;
import hudson.model.Item;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public abstract class AbstractCreateMockLoad extends CLICommand {
    @Option(name = "--fast-rotate", usage = "Enable fast rotation of builds")
    public boolean fastRotate;

    @Argument(index = 0, metaVar = "NAME", usage = "Name of the job to create", required = true)
    public String name;

    @Argument(index = 1, metaVar = "DURATION", usage = "Average Build Duration")
    public Long averageDuration;

    protected final int run() throws Exception {
        Jenkins jenkins = Jenkins.get();
        jenkins.checkPermission(Item.CREATE);

        MockProjectFactory factory = ExtensionList.lookupSingleton(getProjectFactoryClass());
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

    protected abstract Class<? extends MockProjectFactory> getProjectFactoryClass();
}
