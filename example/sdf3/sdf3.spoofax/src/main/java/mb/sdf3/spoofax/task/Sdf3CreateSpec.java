package mb.sdf3.spoofax.task;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.common.util.UncheckedException;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.resource.hierarchical.walk.TrueResourceWalker;
import mb.spoofax.core.language.LanguageScope;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

@LanguageScope
public class Sdf3CreateSpec implements TaskDef<Sdf3CreateSpec.Input, Sdf3Spec> {
    public static class Input implements Serializable {
        public final ResourcePath project;
        public final ResourceKey mainFile;

        public Input(ResourcePath project, ResourceKey mainFile) {
            this.project = project;
            this.mainFile = mainFile;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return project.equals(input.project) && mainFile.equals(input.mainFile);
        }

        @Override public int hashCode() {
            return Objects.hash(project, mainFile);
        }

        @Override public String toString() {
            return "Input{project=" + project + ", mainFile=" + mainFile + '}';
        }
    }

    private final ResourceService resourceService;
    private final Sdf3Parse parse;
    private final Sdf3Desugar desugar;

    @Inject public Sdf3CreateSpec(ResourceService resourceService, Sdf3Parse parse, Sdf3Desugar desugar) {
        this.resourceService = resourceService;
        this.parse = parse;
        this.desugar = desugar;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Sdf3Spec exec(ExecContext context, Input input) throws Exception {
        final Supplier<@Nullable IStrategoTerm> mainModuleAstSupplier = desugar.createSupplier(parse.createAstSupplier(input.mainFile).map(Result::get)); // TODO: use Result
        final ResourceWalker walker = Sdf3Util.createResourceWalker();
        final ResourceMatcher matcher = Sdf3Util.createResourceMatcher();
        final HierarchicalResource project = resourceService.getHierarchicalResource(input.project);
        // Create dependency to project dir, such that this task gets re-executed when an SDF3 file is added/removed.
        context.require(project, ResourceStampers.modifiedDirRec(walker, matcher));
        final ArrayList<Supplier<@Nullable IStrategoTerm>> modulesAstSuppliers;
        try {
            modulesAstSuppliers = project
                .walk(new TrueResourceWalker(), new PathResourceMatcher(new ExtensionsPathMatcher("tmpl", "sdf3")))
                .filter(file -> file.getPath() != input.mainFile) // Filter out main module, as it is supplied separately.
                .map(file -> desugar.createSupplier(parse.createAstSupplier(file.getKey()).map(Result::get))) // TODO: use Result
                .collect(Collectors.toCollection(ArrayList::new));
        } catch(UncheckedException e) {
            throw e.getCause();
        }
        return new Sdf3Spec(mainModuleAstSupplier, ListView.of(modulesAstSuppliers));
    }
}
