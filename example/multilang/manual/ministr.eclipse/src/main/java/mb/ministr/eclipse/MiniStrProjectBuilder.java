package mb.ministr.eclipse;

import mb.spoofax.eclipse.build.SpoofaxProjectBuilder;

public class MiniStrProjectBuilder extends SpoofaxProjectBuilder {
    public static final String id = MiniStrPlugin.pluginId + ".builder";

    public MiniStrProjectBuilder() {
        super(MiniStrPlugin.getComponent());
    }
}
