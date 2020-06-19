package mb.ministr.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandOutput;
import mb.statix.multilang.AnalysisContext;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.tasks.SmlBuildMessages;
import org.metaborg.util.log.Level;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class MStrAnalyzeProject implements TaskDef<MStrAnalyzeProject.Input, CommandOutput> {

    public static class Input implements Serializable {
        private final ResourcePath projectPath;
        private final Level logLevel;

        public Input(ResourcePath projectPath, Level logLevel) {
            this.projectPath = projectPath;
            this.logLevel = logLevel;
        }
    }

    private final SmlBuildMessages buildMessages;
    private final AnalysisContextService analysisContextService;

    @Inject public MStrAnalyzeProject(SmlBuildMessages buildMessages, AnalysisContextService analysisContextService) {
        this.buildMessages = buildMessages;
        this.analysisContextService = analysisContextService;
    }

    @Override public String getId() {
        return MStrAnalyzeProject.class.getCanonicalName();
    }

    @Override
    public CommandOutput exec(ExecContext context, Input input) throws Exception {
        AnalysisContext analysisContext = analysisContextService
            .getAnalysisContext(new ContextId("mini-sdf-str"));

        KeyedMessages messages = context.require(buildMessages
            .createTask(new SmlBuildMessages.Input(input.projectPath, analysisContext, input.logLevel)));

        List<CommandFeedback> output = messages.getAllMessages().stream()
            .flatMap(resourceMessages -> resourceMessages.getValue().stream()
                .map(message -> CommandFeedback.showText(
                    String.format("[%s] %s", message.severity, message.text),
                    Optional.ofNullable(resourceMessages.getKey())
                        .map(ResourceKey::getId)
                        .map(Objects::toString)
                        .orElse(null)
                )))
            .collect(Collectors.toList());

        return CommandOutput.of(output);
    }
}
