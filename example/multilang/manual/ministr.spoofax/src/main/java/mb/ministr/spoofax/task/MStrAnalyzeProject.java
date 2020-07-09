package mb.ministr.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;

import javax.inject.Inject;

public class MStrAnalyzeProject implements TaskDef<ResourcePath, CommandFeedback> {
    private final MStrSmlCheck check;

    @Inject public MStrAnalyzeProject(MStrSmlCheck check) {
        this.check = check;
    }

    @Override public String getId() {
        return MStrAnalyzeProject.class.getCanonicalName();
    }

    @Override
    public CommandFeedback exec(ExecContext context, ResourcePath projectRoot) {
        KeyedMessages messages = context.require(check.createTask(projectRoot));
        return CommandFeedback.of(messages);
    }
}
