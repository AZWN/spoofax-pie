package mb.statix.multilang.pie;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Message;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.AnalysisResults;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.utils.MessageUtils;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.Level;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.AbstractMap;
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
        private final ContextId contextId;
        private final Level logLevel;

        public Input(ResourcePath projectPath, HashSet<LanguageId> languages, ContextId contextId, Level logLevel) {
            this.projectPath = projectPath;
            this.languages = languages;
            this.contextId = contextId;
            this.logLevel = logLevel;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return projectPath.equals(input.projectPath) &&
                languages.equals(input.languages) &&
                contextId.equals(input.contextId);
        }

        @Override public int hashCode() {
            return Objects.hash(projectPath, languages, contextId);
        }

        @Override public String toString() {
            return "Input{" +
                "projectPath=" + projectPath +
                ", languages=" + languages +
                ", contextId=" + contextId +
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

    @Override public KeyedMessages exec(ExecContext context, Input input) {
        final AnalysisResults results = context.require(analyzeProject.createTask(
            new SmlAnalyzeProject.Input(input.projectPath, input.languages, input.contextId, input.logLevel)));

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
        results.finalResult().messages().entrySet().stream()
            .map(e -> new AbstractMap.SimpleEntry<>(MessageUtils.tryGetResourceKey(e.getKey(), results.finalResult().state().unifier()),
                MessageUtils.formatMessage(e.getValue(), e.getKey(), resultUnifier)))
            .collect(Collectors.groupingBy(e -> Optional.ofNullable(e.getKey())))
            .forEach((resourceKey, messages) -> builder.addMessages(resourceKey.orElse(null), messages.stream()
                .map(Map.Entry::getValue).collect(Collectors.toList())));

        // Add empty message sets for keys with no message, to ensure old messages on file are cleared
        results.fileResults().keySet()
            .stream()
            .map(AnalysisResults.FileKey::getResource)
            .forEach(key -> builder.addMessages(key, Iterables2.empty()));

        return builder.build();
    }
}
