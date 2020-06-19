package mb.minisdf;

import mb.constraint.common.ConstraintAnalyzer;
import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.stratego.common.StrategoRuntime;

public class MSdfConstraintAnalyzer extends ConstraintAnalyzer {
    public MSdfConstraintAnalyzer(LoggerFactory loggerFactory, ResourceService resourceService, StrategoRuntime strategoRuntime) {
        super(loggerFactory, resourceService, strategoRuntime, "editor-analyze", false);
    }
}
