package mb.minisdf.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandOutput;
import mb.statix.multilang.AnalysisContextService;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class MSdfAnalyzeProject implements TaskDef<ResourcePath, CommandOutput> {
    private final MSdfSmlCheck check;

    @Inject public MSdfAnalyzeProject(MSdfSmlCheck check) {
        this.check = check;
    }

    @Override public String getId() {
        return MSdfAnalyzeProject.class.getCanonicalName();
    }

    @Override
    public CommandOutput exec(ExecContext context, ResourcePath projectRoot) {
        KeyedMessages messages = context.require(check.createTask(projectRoot));

        List<CommandFeedback> output = messages.getAllMessages().stream()
            .flatMap(resourceMessages -> resourceMessages.getValue().stream()
                .map(message -> CommandFeedback.showText(
                    String.format("[%s] %s", message.severity, message.text),
                    resourceMessages.getKey().getId().toString()
                )))
            .collect(Collectors.toList());

        return CommandOutput.of(output);
    }
}
