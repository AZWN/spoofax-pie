package mb.ministr;

import mb.esv.common.ESVStylingRules;
import mb.resource.hierarchical.HierarchicalResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class MStrStylingRules implements Serializable {
    final ESVStylingRules stylingRules;

    private MStrStylingRules(ESVStylingRules stylingRules) {
        this.stylingRules = stylingRules;
    }

    public static MStrStylingRules fromDefinitionDir(HierarchicalResource definitionDir) {
        final HierarchicalResource resource = definitionDir.appendRelativePath("target/metaborg/editor.esv.af");
        try(final InputStream inputStream = resource.openRead()) {
            final ESVStylingRules stylingRules = ESVStylingRules.fromStream(inputStream);
            return new MStrStylingRules(stylingRules);
        } catch(IOException e) {
            throw new RuntimeException("Cannot create styling rules; cannot read styling rules from '" + resource + "' in classloader resources", e);
        }
    }
}
