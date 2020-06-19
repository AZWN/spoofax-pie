package mb.ministr.spoofax.task;

import mb.jsglr1.common.JSGLR1ParseResult;
import mb.jsglr1.pie.JSGLR1ParseTaskDef;
import mb.ministr.MStrParser;
import mb.spoofax.core.language.LanguageScope;

import javax.inject.Inject;
import javax.inject.Provider;

@LanguageScope
public class MStrParse extends JSGLR1ParseTaskDef {
    private final Provider<MStrParser> parserProvider;

    @Inject public MStrParse(Provider<MStrParser> parserProvider) {
        this.parserProvider = parserProvider;
    }

    @Override public String getId() {
        return MStrParse.class.getCanonicalName();
    }

    @Override
    protected JSGLR1ParseResult parse(String text) throws InterruptedException {
        final MStrParser parser = parserProvider.get();
        return parser.parse(text, "MSTRStart");
    }
}
