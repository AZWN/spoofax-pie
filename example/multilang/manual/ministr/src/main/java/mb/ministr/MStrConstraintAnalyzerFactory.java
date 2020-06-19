package mb.ministr;

import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.spoofax.compiler.interfaces.spoofaxcore.ConstraintAnalyzerFactory;
import mb.stratego.common.StrategoRuntime;

public class MStrConstraintAnalyzerFactory implements ConstraintAnalyzerFactory {
    private final LoggerFactory loggerFactory;
    private final ResourceService resourceService;
    private final StrategoRuntime strategoRuntime;

    public MStrConstraintAnalyzerFactory(LoggerFactory loggerFactory, ResourceService resourceService, StrategoRuntime strategoRuntime) {
        this.loggerFactory = loggerFactory;
        this.resourceService = resourceService;
        this.strategoRuntime = strategoRuntime;
    }

    @Override public MStrConstraintAnalyzer create() {
        return new MStrConstraintAnalyzer(loggerFactory, resourceService, strategoRuntime);
    }
}
