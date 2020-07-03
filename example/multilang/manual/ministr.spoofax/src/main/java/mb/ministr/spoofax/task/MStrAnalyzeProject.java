package mb.ministr.spoofax.task;
import mb.pie.api.Task;

import mb.common.message.KeyedMessages;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandOutput;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.MultiLangConfig;
import mb.statix.multilang.pie.SmlBuildMessages;
import mb.statix.multilang.utils.ContextUtils;
import org.metaborg.util.log.Level;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class MStrAnalyzeProject implements TaskDef<ResourcePath, CommandOutput> {
    private final MStrSmlCheck check;

    @Inject public MStrAnalyzeProject(MStrSmlCheck check) {
        this.check = check;
    }

    @Override public String getId() {
        return MStrAnalyzeProject.class.getCanonicalName();
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
