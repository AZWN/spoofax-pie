package mb.ministr;

import mb.constraint.common.ConstraintAnalyzer;
import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.stratego.common.StrategoRuntime;

public class MStrConstraintAnalyzer extends ConstraintAnalyzer {
    public MStrConstraintAnalyzer(LoggerFactory loggerFactory, ResourceService resourceService, StrategoRuntime strategoRuntime) {
        super(loggerFactory, resourceService, strategoRuntime, "editor-analyze", false);
    }
}
