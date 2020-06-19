package mb.ministr;

import mb.log.api.LoggerFactory;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.interfaces.spoofaxcore.StylerFactory;

public class MStrStylerFactory implements StylerFactory {
    private final MStrStylingRules stylingRules;
    private final LoggerFactory loggerFactory;

    public MStrStylerFactory(LoggerFactory loggerFactory, HierarchicalResource definitionDir) {
        this.stylingRules = MStrStylingRules.fromDefinitionDir(definitionDir);
        this.loggerFactory = loggerFactory;
    }

    @Override public MStrStyler create() {
        return new MStrStyler(stylingRules, loggerFactory);
    }
}
