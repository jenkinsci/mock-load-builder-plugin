package jenkins.plugin.mockloadbuilder;

import hudson.Extension;
import hudson.cli.CLICommand;
import jenkins.model.Jenkins;
import org.kohsuke.args4j.Argument;

@Extension
public class SetMockLoadForkPolicy extends CLICommand {

    @Override
    public String getShortDescription() {
        return "Changes the fork policy of the mock load builder";
    }

    @Argument(index = 0, metaVar = "MODE", usage = "Either `fork` or `nofork`", required = true)
    public String mode;

    protected int run() throws Exception {
        Jenkins jenkins = Jenkins.getActiveInstance();
        jenkins.checkPermission(Jenkins.ADMINISTER);

        MockProjectFactory.mode = "nofork".equalsIgnoreCase(mode);

        if (MockProjectFactory.mode) {
            stdout.println("Mock load will be generated on the agent's JVM without forking");
        } else {
            stdout.println("Mock load will be generated on the agent by forking a JVM");
        }
        return 0;
    }
}
