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
import org.metaborg.util.iterators.Iterables2;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MiniStrMetadataProvider implements LanguageMetadataProvider {

    public LanguageMetadata getLanguageMetadata() {
        MiniStrComponent component = MiniStrPlugin.getComponent();
        MiniStrInstance miniStr = component.getLanguageInstance();

        ResourceKeyString miniStrSpecPath = ResourceKeyString.of("mb/ministr/src-gen/statix");
        ClassLoaderResource miniStrSpec = MStrClassloaderResources
            .createClassLoaderResourceRegistry()
            .getResource(miniStrSpecPath);

        SpecBuilder spec;
        try {
            spec = SpecUtils.loadSpec(miniStrSpec, "mini-str/mini-str-typing", miniStr.termFactory());
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
            .addAllTaskDefs(component.getTaskDefs())
            .addResourceRegistries(MStrClassloaderResources.createClassLoaderResourceRegistry())
            .statixSpec(spec)
            .fileConstraint("mini-str/mini-str-typing!mstrProgramOK")
            .projectConstraint("mini-str/mini-str-typing!mstrProjectOK")
            .build();
    }

    @Override
    public Iterable<Map.Entry<LanguageId, Supplier<LanguageMetadata>>> getLanguageMetadatas() {
        return Iterables2.singleton(new AbstractMap.SimpleEntry<>(
            new LanguageId("mb.ministr"), this::getLanguageMetadata));
    }
}
