package mb.minisdf.eclipse;

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

public class MiniSdfMetadataProvider implements LanguageMetadataProvider {

    private LanguageMetadata getLanguageMetadata() {
        MiniSdfComponent component = MiniSdfPlugin.getComponent();
        MiniSdfInstance miniSdf = component.getLanguageInstance();

        ResourceKeyString miniSdfSpecPath = ResourceKeyString.of("mb/minisdf/src-gen/statix");
        ClassLoaderResource miniSdfSpec = MSdfClassloaderResources
            .createClassLoaderResourceRegistry()
            .getResource(miniSdfSpecPath);

        SpecBuilder spec;
        try {
            spec = SpecUtils.loadSpec(miniSdfSpec, "mini-sdf/mini-sdf-typing", miniSdf.termFactory());
        } catch(IOException e) {
            throw new SpecLoadException(e);
        }

        return ImmutableLanguageMetadata.builder()
            .resourcesSupplier((exec, projectDir) -> {
                HierarchicalResource res = exec.getHierarchicalResource(projectDir);
                try {
                    return res.walk(
                        new PathResourceWalker(new NoHiddenPathMatcher()),
                        new PathResourceMatcher(new ExtensionsPathMatcher(miniSdf.getFileExtensions().asUnmodifiable())))
                        .map(HierarchicalResource::getKey)
                        .collect(Collectors.toCollection(HashSet::new));
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            })
            .astFunction(miniSdf.preStatix().createFunction()
                .mapInput((exec, key) ->  miniSdf.indexAst().createSupplier(key)))
            .postTransform(miniSdf.postStatix().createFunction())
            .languageId(new LanguageId("mb.minisdf"))
            .addAllTaskDefs(component.getTaskDefs())
            .addResourceRegistries(MSdfClassloaderResources.createClassLoaderResourceRegistry())
            .statixSpec(spec)
            .fileConstraint("mini-sdf/mini-sdf-typing!msdfProgramOK")
            .projectConstraint("mini-sdf/mini-sdf-typing!msdfProjectOK")
            .build();
    }

    @Override
    public Iterable<Map.Entry<LanguageId, Supplier<LanguageMetadata>>> getLanguageMetadatas() {
        return Iterables2.singleton(new AbstractMap.SimpleEntry<>(
            new LanguageId("mb.minisdf"), this::getLanguageMetadata));
    }

    @Override
    public Iterable<Map.Entry<ContextId, Iterable<LanguageId>>> getContextConfigurations() {
        return Iterables2.from(
            new AbstractMap.SimpleEntry<>(new ContextId("mini-sdf-str"), Iterables2.singleton(new LanguageId("mb.minisdf"))),
            new AbstractMap.SimpleEntry<>(new ContextId("mini-sdf"), Iterables2.singleton(new LanguageId("mb.minisdf"))));
    }
}
