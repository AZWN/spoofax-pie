package mb.ministr.eclipse;

import mb.statix.multilang.ContextId;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.eclipse.ConstantContextMetadataProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MiniStrContextProvider extends ConstantContextMetadataProvider {

    public MiniStrContextProvider() {
        super(getDefaultContexts());
    }

    private static Map<ContextId, Set<LanguageId>> getDefaultContexts() {
        Set<LanguageId> languageIds = Collections.singleton(new LanguageId("mb.ministr"));
        HashMap<ContextId, Set<LanguageId>> configMap = new HashMap<>();
        configMap.put(new ContextId("mb.ministr"), languageIds);
        configMap.put(new ContextId("mini-sdf-str"), languageIds);
        return configMap;
    }
}
