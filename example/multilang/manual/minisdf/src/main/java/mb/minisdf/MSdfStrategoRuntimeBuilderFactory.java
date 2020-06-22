package mb.minisdf;

import mb.constraint.common.stratego.ConstraintPrimitiveLibrary;
import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.interfaces.spoofaxcore.StrategoRuntimeBuilderFactory;
import mb.stratego.common.StrategoRuntimeBuilder;

public class MSdfStrategoRuntimeBuilderFactory implements StrategoRuntimeBuilderFactory {
    private final LoggerFactory loggerFactory;
    private final ResourceService resourceService;
    private final HierarchicalResource definitionDir;

    public MSdfStrategoRuntimeBuilderFactory(LoggerFactory loggerFactory, ResourceService resourceService, HierarchicalResource definitionDir) {
        this.loggerFactory = loggerFactory;
        this.resourceService = resourceService;
        this.definitionDir = definitionDir;
    }

    @Override
    public StrategoRuntimeBuilder create() {
        final StrategoRuntimeBuilder builder = new StrategoRuntimeBuilder(loggerFactory, resourceService, definitionDir);
        builder.withJarParentClassLoader(MSdfStrategoRuntimeBuilderFactory.class.getClassLoader());
        builder.addCtree(definitionDir.appendRelativePath("target/metaborg/stratego.ctree"));
        builder.addLibrary(new ConstraintPrimitiveLibrary(resourceService));
        return builder;
    }
}
