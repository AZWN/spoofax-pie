package mb.minisdf;

import mb.jsglr1.common.JSGLR1ParseTable;
import mb.jsglr1.common.JSGLR1ParseTableException;
import mb.resource.hierarchical.HierarchicalResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class MSdfParseTable implements Serializable {
    final JSGLR1ParseTable parseTable;

    private MSdfParseTable(JSGLR1ParseTable parseTable) {
        this.parseTable = parseTable;
    }

    public static MSdfParseTable fromDefinitionDir(HierarchicalResource definitionDir) {
        final HierarchicalResource resource = definitionDir.appendRelativePath("target/metaborg/sdf.tbl");
        try(final InputStream inputStream = resource.openRead()) {
            final JSGLR1ParseTable parseTable = JSGLR1ParseTable.fromStream(inputStream);
            return new MSdfParseTable(parseTable);
        } catch(JSGLR1ParseTableException | IOException e) {
            throw new RuntimeException("Cannot create parse table; cannot read parse table from resource '" + resource + "' in classloader resources");
        }
    }
}
