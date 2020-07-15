package mb.statix.multilang;

import java.util.Set;

public interface ContextDataManager {
    Set<LanguageId> getAvailableLanguages();
    Set<LanguageId> getContextLanguages(ContextId contextId);
    ContextId getDefaultContextId(LanguageId languageId);
}