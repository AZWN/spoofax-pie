package mb.minisdf.eclipse;

import mb.spoofax.eclipse.build.SpoofaxProjectBuilder;

public class MiniSdfProjectBuilder extends SpoofaxProjectBuilder {
    public static final String id = MiniSdfPlugin.pluginId + ".builder";

    public MiniSdfProjectBuilder() {
        super(MiniSdfPlugin.getComponent());
    }
}
