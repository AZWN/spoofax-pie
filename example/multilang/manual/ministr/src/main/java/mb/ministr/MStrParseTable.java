package mb.ministr;

import mb.jsglr1.common.JSGLR1ParseTable;
import mb.jsglr1.common.JSGLR1ParseTableException;
import mb.resource.hierarchical.HierarchicalResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class MStrParseTable implements Serializable {
    final JSGLR1ParseTable parseTable;

    private MStrParseTable(JSGLR1ParseTable parseTable) {
        this.parseTable = parseTable;
    }

    public static MStrParseTable fromDefinitionDir(HierarchicalResource definitionDir) {
        final HierarchicalResource resource = definitionDir.appendRelativePath("target/metaborg/sdf.tbl");
        try(final InputStream inputStream = resource.openRead()) {
            final JSGLR1ParseTable parseTable = JSGLR1ParseTable.fromStream(inputStream);
            return new MStrParseTable(parseTable);
        } catch(JSGLR1ParseTableException | IOException e) {
            throw new RuntimeException("Cannot create parse table; cannot read parse table from resource '" + resource + "' in classloader resources");
        }
    }
}
