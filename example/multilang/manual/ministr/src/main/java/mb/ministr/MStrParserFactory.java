package mb.ministr;

import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.interfaces.spoofaxcore.ParserFactory;

public class MStrParserFactory implements ParserFactory {
    private final MStrParseTable parseTable;

    public MStrParserFactory(HierarchicalResource definitionDir) {
        this.parseTable = MStrParseTable.fromDefinitionDir(definitionDir);
    }

    @Override public MStrParser create() {
        return new MStrParser(parseTable);
    }
}
