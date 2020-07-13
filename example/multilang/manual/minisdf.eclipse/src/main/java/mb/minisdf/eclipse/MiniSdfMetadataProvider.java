package mb.minisdf.eclipse;

import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.minisdf.MSdfClassloaderResources;
import mb.minisdf.spoofax.MiniSdfComponent;
import mb.minisdf.spoofax.MiniSdfInstance;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
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
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
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
            // TODO: Remove RuntimeException
            throw new RuntimeException(new SpecLoadException(e));
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
                .mapInput(new IndexAstMapper(miniSdf.indexAst()))
                .mapOutput((exec, out) -> out.ok()))
            .postTransform(miniSdf.postStatix().createFunction())
            .languageId(new LanguageId("mb.minisdf"))
            .languagePie(component.languagePie())
            .termFactory(component.getStrategoRuntime().getTermFactory())
            .statixSpec(spec)
            .fileConstraint("mini-sdf/mini-sdf-typing!msdfProgramOK")
            .projectConstraint("mini-sdf/mini-sdf-typing!msdfProjectOK")
            .build();
    }

    @Override
    public Map<LanguageId, Supplier<LanguageMetadata>> getLanguageMetadataSuppliers() {
        Map<LanguageId, Supplier<LanguageMetadata>> result = new HashMap<>();
        result.put(new LanguageId("mb.minisdf"), this::getLanguageMetadata);
        return result;
    }

    @Override
    public Map<LanguageId, ContextId> getDefaultLanguageContexts() {
        Map<LanguageId, ContextId> result = new HashMap<>();
        result.put(new LanguageId("mb.minisdf"), new ContextId("mini-sdf-str"));
        return result;
    }

    // TODO: Move into library
    private static class IndexAstMapper implements Function<ResourceKey, mb.pie.api.Supplier<Result<IStrategoTerm, JSGLR1ParseException>>> {
        private TaskDef<ResourceKey, Result<IStrategoTerm, JSGLR1ParseException>> indexAstTaskDef;

        public IndexAstMapper(TaskDef<ResourceKey, Result<IStrategoTerm, JSGLR1ParseException>> indexAstTaskDef) {
            this.indexAstTaskDef = indexAstTaskDef;
        }

        @Override
        public mb.pie.api.Supplier<Result<IStrategoTerm, JSGLR1ParseException>> apply(ExecContext context, ResourceKey input) {
            return indexAstTaskDef.createSupplier(input);
        }
    }
}
