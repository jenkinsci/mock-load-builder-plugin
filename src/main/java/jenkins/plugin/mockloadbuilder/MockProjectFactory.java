package jenkins.plugin.mockloadbuilder;

import hudson.ExtensionPoint;
import hudson.model.Job;
import java.io.IOException;
import jenkins.model.ModifiableTopLevelItemGroup;

public abstract class MockProjectFactory implements ExtensionPoint {

    static boolean mode = Boolean.getBoolean("fakeMockLoad");

    public abstract int getFrequency();

    public int getMultiplier() {
        return 1;
    }

    public abstract Job create(ModifiableTopLevelItemGroup ig, String name, Long averageDuration, boolean fastRotate) throws IOException;
}
