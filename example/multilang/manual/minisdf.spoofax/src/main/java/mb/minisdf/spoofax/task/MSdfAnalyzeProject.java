package mb.minisdf.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandOutput;
import mb.statix.multilang.AnalysisContext;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.tasks.SmlBuildMessages;
import org.metaborg.util.log.Level;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class MSdfAnalyzeProject implements TaskDef<ResourcePath, CommandOutput> {
    private final SmlBuildMessages buildMessages;
    private final AnalysisContextService analysisContextService;

    @Inject public MSdfAnalyzeProject(SmlBuildMessages buildMessages, AnalysisContextService analysisContextService) {
        this.buildMessages = buildMessages;
        this.analysisContextService = analysisContextService;
    }

    @Override public String getId() {
        return MSdfAnalyzeProject.class.getCanonicalName();
    }

    @Override
    public CommandOutput exec(ExecContext context, ResourcePath projectRoot) throws Exception {
        AnalysisContext analysisContext = analysisContextService
            .getAnalysisContext(new ContextId("mini-sdf-str"));

        KeyedMessages messages = context.require(buildMessages
            .createTask(new SmlBuildMessages.Input(projectRoot, analysisContext, Level.Warn)));

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
