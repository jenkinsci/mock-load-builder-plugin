package jenkins.plugin.mockloadbuilder;

import hudson.model.Item;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.apache.commons.lang.StringUtils;

public final class Helper {
    public static ModifiableTopLevelItemGroup resolveFolder(String group) {
        Jenkins jenkins = Jenkins.get();
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
        return ig;
    }
}
