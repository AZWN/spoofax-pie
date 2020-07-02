package mb.minisdf.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.pie.api.ExecContext;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageScope;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.ContextConfig;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.pie.SmlBuildContextConfiguration;
import mb.statix.multilang.pie.SmlBuildMessages;
import mb.statix.multilang.pie.SmlLanguageContext;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

@LanguageScope
public class MSdfSmlCheck implements TaskDef<MSdfSmlCheck.Input, KeyedMessages> {
    public static class Input implements Serializable {
        public ResourcePath projectPath;
    }

    @Override
    public String getId() {
        return MSdfSmlCheck.class.getCanonicalName();
    }

    private final SmlBuildContextConfiguration buildContextConfiguration;
    private final SmlBuildMessages buildMessages;
    private final AnalysisContextService analysisContextService;

    @Inject public MSdfSmlCheck(SmlBuildContextConfiguration buildContextConfiguration, SmlBuildMessages buildMessages, AnalysisContextService analysisContextService) {
        this.buildContextConfiguration = buildContextConfiguration;
        this.buildMessages = buildMessages;
        this.analysisContextService = analysisContextService;
    }

    @Override
    public KeyedMessages exec(ExecContext context, Input input) throws Exception {
        // TODO: aggregate parse messages
        final SmlBuildContextConfiguration.Output contextInfo = context.require(buildContextConfiguration.createTask(
            new SmlBuildContextConfiguration.Input(input.projectPath, new LanguageId("mb.minisdf"))
        ));

        final List<LanguageId> languageIds = contextInfo.getContextConfig().getLanguages();
        // We execute the actual analysis in the context of a shared Pie, to make sure all information is present.
        // This will not break incrementality, because this task (or its equivalents for other languages)
        // depends directly on all the files SmlBuildMessages depends on
        Pie sharedPie = analysisContextService.buildPieForLanguages(languageIds);
        try(MixedSession session = sharedPie.newSession()) {
            return session.require(buildMessages.createTask(new SmlBuildMessages.Input(
                input.projectPath,
                languageIds,
                contextInfo.getContextId(),
                contextInfo.getContextConfig().parseLevel()
            )));
        }
    }
}
