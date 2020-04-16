package jenkins.plugin.mockloadbuilder;

import hudson.ExtensionPoint;
import hudson.model.Job;
import java.io.IOException;

import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Fingerprinter;
import hudson.tasks.LogRotator;
import hudson.tasks.junit.JUnitResultArchiver;
import jenkins.model.ModifiableTopLevelItemGroup;

public abstract class MockProjectFactory implements ExtensionPoint {

    static boolean mode = Boolean.getBoolean("fakeMockLoad");

    public abstract int getFrequency();

    public int getMultiplier() {
        return 1;
    }

    public abstract Job create(ModifiableTopLevelItemGroup ig, String name, Long averageDuration, boolean fastRotate) throws IOException;

    protected ArtifactArchiver createArtifactArchiver() {
        ArtifactArchiver artifactArc = new ArtifactArchiver("mock-artifact-*.txt");
        artifactArc.setAllowEmptyArchive(true);
        return artifactArc;
    }

    protected JUnitResultArchiver createJunitArchiver() {
        return new JUnitResultArchiver("mock-junit.xml");
    }

    protected Fingerprinter createFingerprinter() {
        return new Fingerprinter("mock-artifact-*.txt");
    }

    protected MockLoadBuilder createMockLoadBuilder(Long averageDuration) {
        return new MockLoadBuilder(averageDuration == null || averageDuration < 0 ? 60L : averageDuration);
    }

    protected LogRotator createBuildDiscarder(boolean fastRotate) {
        return fastRotate ? new LogRotator(-1, 5, -1, 1) : new LogRotator(30, 100, 10, 33);
    }

    public abstract String getName();
}
