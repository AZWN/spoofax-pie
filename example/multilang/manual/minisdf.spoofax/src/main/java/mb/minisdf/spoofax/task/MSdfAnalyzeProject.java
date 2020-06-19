package mb.minisdf.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandOutput;
import mb.statix.multilang.AnalysisContext;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.MultiLangConfig;
import mb.statix.multilang.tasks.SmlBuildMessages;
import mb.statix.multilang.utils.ContextUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class MSdfAnalyzeProject implements TaskDef<ResourcePath, CommandOutput> {
    private final SmlBuildMessages buildMessages;
    private final AnalysisContextService analysisContextService;
    private final ResourceService resourceService;

    @Inject public MSdfAnalyzeProject(SmlBuildMessages buildMessages, AnalysisContextService analysisContextService, ResourceService resourceService) {
        this.buildMessages = buildMessages;
        this.analysisContextService = analysisContextService;
        this.resourceService = resourceService;
    }

    @Override public String getId() {
        return MSdfAnalyzeProject.class.getCanonicalName();
    }

    @Override
    public CommandOutput exec(ExecContext context, ResourcePath projectRoot) {
        MultiLangConfig config = ContextUtils.readYamlConfig(resourceService, projectRoot);
        String contextId = config.getLanguageContexts().getOrDefault("mb.minisdf", "mini-sdf-str");
        AnalysisContext analysisContext = analysisContextService
            .getAnalysisContext(new ContextId(contextId));

        KeyedMessages messages = context.require(buildMessages
            .createTask(new SmlBuildMessages.Input(projectRoot, analysisContext)));

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
