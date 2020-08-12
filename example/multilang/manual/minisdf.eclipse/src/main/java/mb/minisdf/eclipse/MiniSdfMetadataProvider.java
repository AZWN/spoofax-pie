package mb.minisdf.eclipse;

import mb.statix.multilang.metadata.ContextId;
import mb.statix.multilang.metadata.LanguageId;
import mb.statix.multilang.metadata.LanguageMetadata;
import mb.statix.multilang.eclipse.LanguageMetadataProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MiniSdfMetadataProvider implements LanguageMetadataProvider {

    // DO not inline this method into getLanguageMetadataSuppliers
    // Because call of getComponent should be delayed for concurrency reasons
    private LanguageMetadata getLanguageMetadata() {
        return MiniSdfPlugin.getComponent().getLanguageMetadata();
    }

    @Override
    public Map<LanguageId, Supplier<LanguageMetadata>> getLanguageMetadataSuppliers() {
        Map<LanguageId, Supplier<LanguageMetadata>> result = new HashMap<>();
        result.put(new LanguageId("mb.minisdf"), this::getLanguageMetadata);
        return result;
    }

    @Override
    public Map<LanguageId, ContextId> getDefaultLanguageContexts() {
        Map<LanguageId, ContextId> result = new HashMap<>();
        result.put(new LanguageId("mb.minisdf"), new ContextId("mini-sdf-str"));
        return result;
    }
}
