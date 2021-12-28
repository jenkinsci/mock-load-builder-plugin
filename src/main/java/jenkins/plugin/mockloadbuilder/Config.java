package jenkins.plugin.mockloadbuilder;

import hudson.Extension;
import hudson.ExtensionList;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.jenkinsci.Symbol;

@Symbol("mockLoad")
@Extension
public class Config extends GlobalConfiguration {
    private int freestyleFrequency = 45;
    private int pipelineFrequency = 50;
    private int matrixFrequency = 5;
    private int matrixMultiplier = 4;

    public Config() {
        super();
        load();
    }

    public static Config get() {
        return ExtensionList.lookupSingleton(Config.class);
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        boolean result = super.configure(req, json);
        save();
        return result;
    }

    public int getFreestyleFrequency() {
        return freestyleFrequency;
    }

    @DataBoundSetter
    public void setFreestyleFrequency(int freestyleFrequency) {
        this.freestyleFrequency = freestyleFrequency;
    }

    public int getPipelineFrequency() {
        return pipelineFrequency;
    }

    @DataBoundSetter
    public void setPipelineFrequency(int pipelineFrequency) {
        this.pipelineFrequency = pipelineFrequency;
    }

    public int getMatrixFrequency() {
        return matrixFrequency;
    }

    @DataBoundSetter
    public void setMatrixFrequency(int matrixFrequency) {
        this.matrixFrequency = matrixFrequency;
    }

    public int getMatrixMultiplier() {
        return matrixMultiplier;
    }

    @DataBoundSetter
    public void setMatrixMultiplier(int matrixMultiplier) {
        this.matrixMultiplier = matrixMultiplier;
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "Mock Load Builder";
    }


}
