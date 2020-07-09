package mb.ministr;

import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.jsglr1.common.JSGLR1Parser;
import mb.resource.ResourceKey;
import mb.spoofax.compiler.interfaces.spoofaxcore.Parser;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MStrParser implements Parser {
    private final JSGLR1Parser parser;

    public MStrParser(MStrParseTable parseTable) {
        this.parser = new JSGLR1Parser(parseTable.parseTable);
    }

    @Override
    public JSGLR1ParseOutput parse(String text, String startSymbol) throws InterruptedException, JSGLR1ParseException {
        return parser.parse(text, startSymbol, null);
    }

    @Override
    public JSGLR1ParseOutput parse(String text, String startSymbol, @Nullable ResourceKey resource) throws InterruptedException, JSGLR1ParseException {
        return parser.parse(text, startSymbol, resource);
    }
}
