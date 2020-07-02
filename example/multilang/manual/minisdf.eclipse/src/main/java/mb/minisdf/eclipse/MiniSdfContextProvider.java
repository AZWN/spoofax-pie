package mb.minisdf.eclipse;

import mb.statix.multilang.ContextId;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.eclipse.ConstantContextMetadataProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MiniSdfContextProvider extends ConstantContextMetadataProvider {

    public MiniSdfContextProvider() {
        super(getDefaultContexts());
    }

    private static Map<ContextId, Set<LanguageId>> getDefaultContexts() {
        Set<LanguageId> languageIdSet = Collections.singleton(new LanguageId("mb.minisdf"));
        HashMap<ContextId, Set<LanguageId>> configMap = new HashMap<>();
        configMap.put(new ContextId("mb.minisdf"), languageIdSet);
        configMap.put(new ContextId("mini-sdf-str"), languageIdSet);
        return configMap;
    }
}
