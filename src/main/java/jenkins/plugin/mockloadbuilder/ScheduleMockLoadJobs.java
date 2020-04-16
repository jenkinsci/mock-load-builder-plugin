package jenkins.plugin.mockloadbuilder;

import hudson.Extension;
import hudson.cli.CLICommand;
import hudson.model.BuildableItem;
import hudson.model.Cause;
import hudson.model.Item;
import hudson.model.User;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.kohsuke.args4j.Argument;

import java.util.Collections;
import java.util.List;

@Extension
public class ScheduleMockLoadJobs extends CLICommand {

    @Argument(index = 0, metaVar = "COUNT", usage = "Number of jobs to create", required = true)
    public Integer count;

    @Argument(index = 1, metaVar = "FOLDER", usage = "Where to create the jobs")
    public String group;

    @Override
    public String getShortDescription() {
        return "Schedule N mock jobs in the given folder";
    }

    @Override
    protected int run() throws Exception {
        Jenkins.get().checkPermission(Item.BUILD);
        User currentUser = User.current();
        ModifiableTopLevelItemGroup ig = Helper.resolveFolder(group);
        int index = 0;
        List<BuildableItem> allItems = ig.getAllItems(BuildableItem.class);
        Collections.shuffle(allItems);
        for (BuildableItem i : allItems) {
            if (index < count && i.getName().startsWith(CreateMockLoadJobs.NAME_PREFIX)) {
                i.scheduleBuild(new Cause.UserIdCause(currentUser == null ? null : currentUser.getId()));
                stdout.println("Scheduled " + i.getFullName());
                index++;
            }
        }
        return 0;
    }
}
