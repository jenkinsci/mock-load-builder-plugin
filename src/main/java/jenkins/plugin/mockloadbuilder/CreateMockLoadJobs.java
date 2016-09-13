package jenkins.plugin.mockloadbuilder;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.cli.CLICommand;
import hudson.model.Computer;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Fingerprinter;
import hudson.tasks.LogRotator;
import hudson.tasks.junit.JUnitResultArchiver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.args4j.Argument;

import java.util.Random;
import org.kohsuke.args4j.Option;

@Extension
public class CreateMockLoadJobs extends CLICommand {
    @Override
    public String getShortDescription() {
        return "Creates a set of jobs that generate a mock load";
    }

    @Option(name = "--fast-rotate", usage = "Enable fast rotation of builds")
    public boolean fastRotate;

    @Argument(index = 0, metaVar = "COUNT", usage = "Number of jobs to create", required = true)
    public Integer count;

    @Argument(index = 1, metaVar = "DURATION", usage = "Average build duration, -1 will give a typical random duration to each job", required = false)
    public Long averageDuration;

    @Argument(index = 2, metaVar = "FOLDER", usage = "Where to create the jobs", required = false)
    public String group;

    protected int run() throws Exception {
        Jenkins jenkins = Jenkins.getActiveInstance();
        jenkins.checkPermission(Item.CREATE);

        ModifiableTopLevelItemGroup ig = jenkins;
        if (StringUtils.isNotBlank(group)) {
            Item item = jenkins.getItemByFullName(group);
            if (item == null) {
                throw new IllegalArgumentException("Unknown ItemGroup " + group);
            }

            if (item instanceof ModifiableTopLevelItemGroup) {
                ig = (ModifiableTopLevelItemGroup) item;
            } else {
                throw new IllegalArgumentException("Can't create job from CLI in " + group);
            }
        }

        List<MockProjectFactory> factories = new ArrayList<>(100);
        for (MockProjectFactory f : ExtensionList.lookup(MockProjectFactory.class)) {
            for (int i = f.getFrequency(); i > 0; i--) {
                factories.add(f);
            }
        }
        Random entropy = new Random();
        Collections.shuffle(factories, entropy);

        if (averageDuration == null || averageDuration < 0) averageDuration = 60L;
        long sumDuration = 0;
        int countDuration = 0;
        int index = 0;
        double multiplier = 1;
        for (int n = 0; n < count; n++) {
            String name = "mock-load-job-" + StringUtils.leftPad(Integer.toString(n+1), 5, '0');
            if (ig.getItem(name) != null) {
                continue;
            }
            Jenkins.checkGoodName(name);

            if (index >= factories.size()) {
                index = 0;
            }
            MockProjectFactory factory = factories.get(index++);

            // 1.649 normalizes the expected mean back to 1
            long duration = (long) (averageDuration * Math.exp(entropy.nextGaussian()) / 1.649);
            factory.create(ig, name, duration, fastRotate);
            stdout.println("Created " + name + " with average duration " + duration + "s");
            sumDuration+=duration*factory.getMultiplier();
            countDuration+=factory.getMultiplier();
            multiplier = (multiplier * n + factory.getMultiplier()) / (n + 1.0);
        }
        if (countDuration > 0)
        stdout.printf("Overall average duration: %ds%n",(sumDuration / countDuration));
        stdout.printf("Expected executor multiplier: %.1f x (number of builds scheduled per minute)%n",
                (sumDuration / 60.0 / countDuration * multiplier));
        int executorCount = 0;
        for (Computer c: jenkins.getComputers()) {
            executorCount+=c.getNumExecutors();
        }
        stdout.printf("Current ideal max build rate: %.1f builds per minute%n",
                Math.floor(executorCount / (sumDuration / 60.0 / countDuration * multiplier)));

        return 0;
    }
}
