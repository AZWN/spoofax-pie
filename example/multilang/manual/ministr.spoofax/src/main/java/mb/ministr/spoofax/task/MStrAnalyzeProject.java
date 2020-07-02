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
    private final ResourceService resourceService;

    @Inject public MStrAnalyzeProject(SmlBuildMessages buildMessages, AnalysisContextService analysisContextService, ResourceService resourceService) {
        this.buildMessages = buildMessages;
        this.analysisContextService = analysisContextService;
        this.resourceService = resourceService;
    }

    @Override public String getId() {
        return MStrAnalyzeProject.class.getCanonicalName();
    }

    @Override
    public CommandOutput exec(ExecContext context, Input input) throws Exception {
        MultiLangConfig config = ContextUtils.readYamlConfig(resourceService, input.projectPath);
        ContextId contextId = config.getLanguageContexts().getOrDefault(new LanguageId("mb.ministr"), new ContextId("mini-sdf-str"));
        AnalysisContext analysisContext = analysisContextService
            .getAnalysisContext(contextId);

        KeyedMessages messages = context.require(buildMessages
            .createTask(new SmlBuildMessages.Input(input.projectPath, analysisContext)));

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
