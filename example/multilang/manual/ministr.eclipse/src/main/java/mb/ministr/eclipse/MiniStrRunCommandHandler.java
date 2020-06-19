package mb.ministr.eclipse;

import mb.spoofax.eclipse.command.RunCommandHandler;

public class MiniStrRunCommandHandler extends RunCommandHandler {
    public MiniStrRunCommandHandler() {
        super(MiniStrPlugin.getComponent());
    }
}
