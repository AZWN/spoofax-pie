package mb.ministr.eclipse;

import com.google.common.collect.Lists;
import mb.statix.multilang.ContextConfig;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.eclipse.ConstantContextMetadataProvider;

import java.util.HashMap;
import java.util.Map;

public class MiniStrContextProvider extends ConstantContextMetadataProvider {

    public MiniStrContextProvider() {
        super(getDefaultContexts());
    }

    private static Map<ContextId, ContextConfig> getDefaultContexts() {
        ContextConfig config = new ContextConfig();
        config.setLanguages(Lists.newArrayList(new LanguageId("mb.ministr")));
        HashMap<ContextId, ContextConfig> configMap = new HashMap<>();
        configMap.put(new ContextId("ministr"), config);
        configMap.put(new ContextId("mini-sdf-str"), config);
        return configMap;
    }
}
