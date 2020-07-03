package mb.ministr.eclipse;

import mb.ministr.MStrClassloaderResources;
import mb.ministr.spoofax.MiniStrComponent;
import mb.ministr.spoofax.MiniStrInstance;
import mb.resource.ResourceKeyString;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.resource.hierarchical.match.path.NoHiddenPathMatcher;
import mb.resource.hierarchical.walk.PathResourceWalker;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.ImmutableLanguageMetadata;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadata;
import mb.statix.multilang.eclipse.LanguageMetadataProvider;
import mb.statix.multilang.spec.SpecBuilder;
import mb.statix.multilang.spec.SpecLoadException;
import mb.statix.multilang.spec.SpecUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MiniStrMetadataProvider implements LanguageMetadataProvider {

    private LanguageMetadata getLanguageMetadata() {
        MiniStrComponent component = MiniStrPlugin.getComponent();
        MiniStrInstance miniStr = component.getLanguageInstance();

        ResourceKeyString miniSdfSpecPath = ResourceKeyString.of("mb/ministr/src-gen/statix");
        ClassLoaderResource miniSdfSpec = MStrClassloaderResources
            .createClassLoaderResourceRegistry()
            .getResource(miniSdfSpecPath);

        SpecBuilder spec;
        try {
            spec = SpecUtils.loadSpec(miniSdfSpec, "mini-str/mini-str-typing", miniStr.termFactory());
        } catch(IOException e) {
            throw new SpecLoadException(e);
        }

        return ImmutableLanguageMetadata.builder()
            .resourcesSupplier((exec, projectDir) -> {
                HierarchicalResource res = exec.getHierarchicalResource(projectDir);
                try {
                    return res.walk(
                        new PathResourceWalker(new NoHiddenPathMatcher()),
                        new PathResourceMatcher(new ExtensionsPathMatcher(miniStr.getFileExtensions().asUnmodifiable())))
                        .map(HierarchicalResource::getKey)
                        .collect(Collectors.toCollection(HashSet::new));
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            })
            .astFunction(miniStr.preStatix().createFunction()
                .mapInput((exec, key) ->  miniStr.indexAst().createSupplier(key)))
            .postTransform(miniStr.postStatix().createFunction())
            .languageId(new LanguageId("mb.ministr"))
            .languagePie(component.languagePie())
            .termFactory(component.getStrategoRuntime().getTermFactory())
            .statixSpec(spec)
            .fileConstraint("mini-str/mini-str-typing!mstrProgramOK")
            .projectConstraint("mini-str/mini-str-typing!mstrProjectOK")
            .build();
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
