package mb.minisdf;

import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.interfaces.spoofaxcore.ParserFactory;

public class MSdfParserFactory implements ParserFactory {
    private final MSdfParseTable parseTable;

    public MSdfParserFactory(HierarchicalResource definitionDir) {
        this.parseTable = MSdfParseTable.fromDefinitionDir(definitionDir);
    }

    @Override public MSdfParser create() {
        return new MSdfParser(parseTable);
    }
}
