package mb.minisdf;

import mb.log.api.LoggerFactory;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.interfaces.spoofaxcore.StylerFactory;

public class MSdfStylerFactory implements StylerFactory {
    private final MSdfStylingRules stylingRules;
    private final LoggerFactory loggerFactory;

    public MSdfStylerFactory(LoggerFactory loggerFactory, HierarchicalResource definitionDir) {
        this.stylingRules = MSdfStylingRules.fromDefinitionDir(definitionDir);
        this.loggerFactory = loggerFactory;
    }

    @Override public MSdfStyler create() {
        return new MSdfStyler(stylingRules, loggerFactory);
    }
}
