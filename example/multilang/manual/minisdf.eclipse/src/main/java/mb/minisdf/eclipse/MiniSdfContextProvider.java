package mb.minisdf.eclipse;

import com.google.common.collect.Lists;
import mb.minisdf.MSdfClassloaderResources;
import mb.minisdf.spoofax.MiniSdfComponent;
import mb.minisdf.spoofax.MiniSdfInstance;
import mb.resource.ResourceKeyString;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.resource.hierarchical.match.path.NoHiddenPathMatcher;
import mb.resource.hierarchical.walk.PathResourceWalker;
import mb.statix.multilang.ContextConfig;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.ImmutableLanguageMetadata;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadata;
import mb.statix.multilang.eclipse.ConstantContextMetadataProvider;
import mb.statix.multilang.eclipse.LanguageMetadataProvider;
import mb.statix.multilang.spec.SpecBuilder;
import mb.statix.multilang.spec.SpecLoadException;
import mb.statix.multilang.spec.SpecUtils;
import org.metaborg.util.iterators.Iterables2;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MiniSdfContextProvider extends ConstantContextMetadataProvider {

    public MiniSdfContextProvider() {
        super(getDefaultContexts());
    }

    private static Map<ContextId, Iterable<ContextConfig>> getDefaultContexts() {
        ContextConfig config = new ContextConfig();
        config.setLanguages(Lists.newArrayList("mb.minisdf"));
        HashMap<ContextId, Iterable<ContextConfig>> configMap = new HashMap<>();
        configMap.put(new ContextId("minisdf"), Lists.newArrayList(config));
        configMap.put(new ContextId("mini-sdf-str"), Lists.newArrayList(config));
        return configMap;
    }
}
