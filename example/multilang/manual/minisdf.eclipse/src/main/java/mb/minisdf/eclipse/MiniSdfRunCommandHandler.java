package mb.minisdf.eclipse;

import mb.spoofax.eclipse.command.RunCommandHandler;

public class MiniSdfRunCommandHandler extends RunCommandHandler {
    public MiniSdfRunCommandHandler() {
        super(MiniSdfPlugin.getComponent());
    }
}
