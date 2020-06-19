package mb.minisdf.eclipse;

import mb.spoofax.eclipse.nature.SpoofaxNature;

public class MiniSdfNature extends SpoofaxNature {
    public static final String id = MiniSdfPlugin.pluginId + ".nature";

    public MiniSdfNature() {
        super(MiniSdfPlugin.getComponent());
    }
}
