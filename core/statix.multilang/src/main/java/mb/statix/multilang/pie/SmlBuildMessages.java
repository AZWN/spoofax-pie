package mb.statix.multilang.pie;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Message;
import mb.common.result.Result;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.AnalysisResults;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.utils.MessageUtils;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.Level;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@MultiLangScope
public class SmlBuildMessages implements TaskDef<SmlBuildMessages.Input, KeyedMessages> {

    public static class Input implements Serializable {
        private final ResourcePath projectPath;
        private final HashSet<LanguageId> languages;
        private final Level logLevel;

        public Input(ResourcePath projectPath, Collection<LanguageId> languages, Level logLevel) {
            this.projectPath = projectPath;
            this.languages = new HashSet<>(languages);
            this.logLevel = logLevel;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return projectPath.equals(input.projectPath) &&
                languages.equals(input.languages);
        }

        @Override public int hashCode() {
            return Objects.hash(projectPath, languages);
        }

        @Override public String toString() {
            return "Input{" +
                "projectPath=" + projectPath +
                ", languages=" + languages +
                '}';
        }
    }

    private final SmlAnalyzeProject analyzeProject;

    @Inject public SmlBuildMessages(SmlAnalyzeProject analyzeProject) {
        this.analyzeProject = analyzeProject;
    }

    @Override public String getId() {
        return SmlBuildMessages.class.getSimpleName();
    }

    @Override public KeyedMessages exec(ExecContext context, Input input) throws IOException {
        // Make sure to depend on configuration, to work around issue with different Pie instances
        context.require(input.projectPath.appendRelativePath("multilang.yaml"), context.getDefaultRequireReadableResourceStamper());

        Task<Result<AnalysisResults, MultiLangAnalysisException>> analyzeTask = analyzeProject
            .createTask(new SmlAnalyzeProject.Input(input.projectPath, input.languages, input.logLevel));
        return context.require(analyzeTask).mapOrElse(results -> {
            final IUniDisunifier resultUnifier = results.finalResult().state().unifier();
            final KeyedMessagesBuilder builder = new KeyedMessagesBuilder();

            // Add all file messages
            results.fileResults().forEach((key, fileResult) -> {
                List<Message> resourceMessages = fileResult.getResult().messages().entrySet().stream()
                    .map(e -> MessageUtils.formatMessage(e.getValue(), e.getKey(), resultUnifier))
                    .collect(Collectors.toList());
                if(!resourceMessages.isEmpty()) {
                    builder.addMessages(key.getResource(), resourceMessages);
                }
            });

            // Process final result messages
            final Map<Boolean, List<AbstractMap.SimpleEntry<ResourceKey, Message>>> messagesKeyPartitioning = results.finalResult().messages().entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(MessageUtils.tryGetResourceKey(e.getKey(), results.finalResult().state().unifier()),
                    MessageUtils.formatMessage(e.getValue(), e.getKey(), resultUnifier)))
                .collect(Collectors.partitioningBy(Objects::isNull));

            // Add all messages without key to project resource
            messagesKeyPartitioning.get(true)
                .stream()
                .map(Map.Entry::getValue)
                .forEach(message -> builder.addMessage(message, input.projectPath));

            // Add all messages with remaining key
            messagesKeyPartitioning.get(false).forEach(entry -> builder.addMessage(entry.getValue(), entry.getKey()));

            // Add empty message sets for keys with no message, to ensure old messages on file are cleared
            builder.addMessages(input.projectPath, Collections.emptySet());
            results.fileResults().keySet()
                .stream()
                .map(AnalysisResults.FileKey::getResource)
                .forEach(key -> builder.addMessages(key, Iterables2.empty()));

            return builder.build();
        }, MultiLangAnalysisException::toKeyedMessages);
    }
}
