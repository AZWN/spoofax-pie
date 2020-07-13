package mb.ministr.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageScope;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.pie.SmlBuildContextConfiguration;
import mb.statix.multilang.pie.SmlBuildMessages;
import mb.statix.multilang.pie.TaskUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@LanguageScope
public class MStrSmlCheck implements TaskDef<ResourcePath, KeyedMessages> {
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
    public String getId() {
        return MStrSmlCheck.class.getCanonicalName();
    }

    @Override
    public KeyedMessages exec(ExecContext context, ResourcePath projectPath) {
        // Aggregate all parse messages
        final KeyedMessagesBuilder builder = new KeyedMessagesBuilder();
        analysisContextService.getLanguageMetadataResult(new LanguageId("mb.ministr"))
            .ifElse(
                languageMetadata -> languageMetadata
                    .resourcesSupplier()
                    .apply(context, projectPath)
                    .forEach(resourceKey -> {
                        try {
                            Messages messages = context.require(parse.createMessagesSupplier(resourceKey));
                            builder.addMessages(resourceKey, messages);
                        } catch(IOException e) {
                            builder.addMessage("IO Exception when parsing file", e, Severity.Error, resourceKey);
                        }
                    }),
                err -> builder.addMessages(err.toKeyedMessages())
            );

        // Aggregate all Analysis Messages
        return context.require(buildContextConfiguration.createTask(new SmlBuildContextConfiguration.Input(projectPath, new LanguageId("mb.ministr"))))
            .mapErr(MultiLangAnalysisException::new)
            .flatMap(contextInfo -> {
                final List<LanguageId> languageIds = contextInfo.getContextConfig().getLanguages();
                // We execute the actual analysis in the context of a shared Pie, to make sure all information is present.
                // This will not break incrementality, because this task (or its equivalents for other languages)
                // depends directly on all the files SmlBuildMessages depends on:
                // - language source files:     via parse tasks
                // - multilang.yaml:            via buildContextConfiguration tasks
                try {
                    final Pie sharedPie = analysisContextService.buildPieForLanguages(languageIds);
                    try(MixedSession session = sharedPie.newSession()) {
                        final Task<KeyedMessages> messagesTask = buildMessages.createTask(new SmlBuildMessages.Input(
                            projectPath,
                            languageIds,
                            contextInfo.getContextConfig().parseLevel()
                        ));
                        return TaskUtils.executeWrapped(() -> Result.ofOk(session.require(messagesTask)), "Exception executing analysis");
                    }
                } catch(MultiLangAnalysisException e) {
                    return Result.ofErr(e);
                }
            })
            .mapOrElse((KeyedMessages messages) -> {
                builder.addMessages(messages);
                return builder.build();
            }, MultiLangAnalysisException::toKeyedMessages);
    }
}
