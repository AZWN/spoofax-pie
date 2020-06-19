package mb.ministr.eclipse;

import com.google.common.collect.Lists;
import mb.statix.multilang.ContextConfig;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.eclipse.ConstantContextMetadataProvider;

import java.util.HashMap;
import java.util.Map;

public class MiniStrContextProvider extends ConstantContextMetadataProvider {

    public MiniStrContextProvider() {
        super(getDefaultContexts());
    }

    private static Map<ContextId, Iterable<ContextConfig>> getDefaultContexts() {
        ContextConfig config = new ContextConfig();
        config.setLanguages(Lists.newArrayList("mb.ministr"));
        HashMap<ContextId, Iterable<ContextConfig>> configMap = new HashMap<>();
        configMap.put(new ContextId("ministr"), Lists.newArrayList(config));
        configMap.put(new ContextId("mini-sdf-str"), Lists.newArrayList(config));
        return configMap;
    }
}
