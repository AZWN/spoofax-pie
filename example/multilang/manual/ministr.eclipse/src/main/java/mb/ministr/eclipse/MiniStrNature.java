package mb.ministr.eclipse;

import mb.spoofax.eclipse.nature.SpoofaxNature;

public class MiniStrNature extends SpoofaxNature {
    public static final String id = MiniStrPlugin.pluginId + ".nature";

    public MiniStrNature() {
        super(MiniStrPlugin.getComponent());
    }
}
