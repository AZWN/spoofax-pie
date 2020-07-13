package mb.ministr.eclipse;

import mb.statix.multilang.ContextId;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadata;
import mb.statix.multilang.eclipse.LanguageMetadataProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MiniStrMetadataProvider implements LanguageMetadataProvider {

    private LanguageMetadata getLanguageMetadata() {
        return MiniStrPlugin.getComponent().getLanguageMetadata();
    }

    @Override
    public Map<LanguageId, Supplier<LanguageMetadata>> getLanguageMetadataSuppliers() {
        Map<LanguageId, Supplier<LanguageMetadata>> result = new HashMap<>();
        result.put(new LanguageId("mb.ministr"), this::getLanguageMetadata);
        return result;
    }

    @Override
    public Map<LanguageId, ContextId> getDefaultLanguageContexts() {
        Map<LanguageId, ContextId> result = new HashMap<>();
        result.put(new LanguageId("mb.ministr"), new ContextId("mini-sdf-str"));
        return result;
    }
}
