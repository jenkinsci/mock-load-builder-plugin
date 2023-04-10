package jenkins.plugin.mockloadbuilder;

import hudson.Extension;
import hudson.cli.CLICommand;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import java.io.IOException;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.kohsuke.args4j.Argument;

@Extension
public class ClearMockLoadJobs extends CLICommand {
    @Argument(index = 0, metaVar = "FOLDER", usage = "Where to create the jobs")
    public String group;

    @Override
    public String getShortDescription() {
        return "Clear previously created mock jobs using create-mock-load-jobs";
    }

    @Override
    protected int run() throws Exception {
        Jenkins jenkins = Jenkins.get();
        jenkins.checkPermission(Item.DELETE);

        ModifiableTopLevelItemGroup ig = Helper.resolveFolder(group);
        boolean error = false;
        int count = 0;
        stdout.println("Deleting mock jobs");
        for (TopLevelItem i : ig.getItems()) {
            String name = i.getName();
            try {
                if (name.startsWith(CreateMockLoadJobs.NAME_PREFIX)) {
                    i.delete();
                    count++;
                }
            } catch (IOException e) {
                stderr.println("Failed to delete " + name);
                error = true;
            }
        }
        stdout.println("Deleted " + count + " jobs.");
        if (error) {
            stderr.println("Failed to delete some jobs.");
        }
        return error ? 1 : 0;
    }
}
