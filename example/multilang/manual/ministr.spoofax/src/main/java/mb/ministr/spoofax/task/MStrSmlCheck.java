package mb.ministr.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageScope;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.pie.SmlBuildContextConfiguration;
import mb.statix.multilang.pie.SmlBuildMessages;

import javax.inject.Inject;
import java.util.List;

@LanguageScope
public class MStrSmlCheck implements TaskDef<ResourcePath, KeyedMessages> {
    @Override
    public String getId() {
        return MStrSmlCheck.class.getCanonicalName();
    }

    private final MStrParse parse;
    private final SmlBuildContextConfiguration buildContextConfiguration;
    private final SmlBuildMessages buildMessages;
    private final AnalysisContextService analysisContextService;

    @Inject public MStrSmlCheck(MStrParse parse, SmlBuildContextConfiguration buildContextConfiguration, SmlBuildMessages buildMessages, AnalysisContextService analysisContextService) {
        this.parse = parse;
        this.buildContextConfiguration = buildContextConfiguration;
        this.buildMessages = buildMessages;
        this.analysisContextService = analysisContextService;
    }

    @Override
    public KeyedMessages exec(ExecContext context, ResourcePath projectPath) throws Exception {
        // Aggregate all parse messages
        final KeyedMessagesBuilder builder = new KeyedMessagesBuilder();
        analysisContextService.getLanguageMetadata(new LanguageId("mb.ministr"))
            .resourcesSupplier()
            .apply(context, projectPath)
            .stream()
            .map(ResourceStringSupplier::new)
            .map(parse::createTask)
            .map(context::require)
            .map(JSGLR1ParseResult::getMessages)
            .forEach(builder::addMessages);

        // Aggregate all Analysis Messages
        final SmlBuildContextConfiguration.Output contextInfo = context.require(buildContextConfiguration.createTask(
            new SmlBuildContextConfiguration.Input(projectPath, new LanguageId("mb.ministr"))
        ));

        final List<LanguageId> languageIds = contextInfo.getContextConfig().getLanguages();
        // We execute the actual analysis in the context of a shared Pie, to make sure all information is present.
        // This will not break incrementality, because this task (or its equivalents for other languages)
        // depends directly on all the files SmlBuildMessages depends on:
        // - language source files:     via parse tasks
        // - multilang.yaml:            via buildContextConfiguration tasks
        final Pie sharedPie = analysisContextService.buildPieForLanguages(languageIds);
        try(MixedSession session = sharedPie.newSession()) {
            builder.addMessages(session.require(buildMessages.createTask(new SmlBuildMessages.Input(
                projectPath,
                languageIds,
                contextInfo.getContextConfig().parseLevel()
            ))));
        }
        return builder.build();
    }
}
