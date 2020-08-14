package mb.ministr.eclipse;

import mb.statix.multilang.metadata.ContextId;
import mb.statix.multilang.metadata.LanguageId;
import mb.statix.multilang.metadata.LanguageMetadata;
import mb.statix.multilang.eclipse.LanguageMetadataProvider;
import mb.statix.multilang.metadata.SpecFragmentId;
import mb.statix.multilang.metadata.spec.SpecConfig;

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
    public Map<SpecFragmentId, SpecConfig> getSpecConfigs() {
        return MiniStrPlugin.getComponent().getSpecConfigs();
    }

    @Override
    public Map<LanguageId, ContextId> getDefaultLanguageContexts() {
        Map<LanguageId, ContextId> result = new HashMap<>();
        result.put(new LanguageId("mb.ministr"), new ContextId("mini-sdf-str"));
        return result;
    }
}
