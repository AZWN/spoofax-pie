package mb.minisdf.spoofax.task;

import mb.jsglr1.common.JSGLR1ParseResult;
import mb.jsglr1.pie.JSGLR1ParseTaskDef;
import mb.minisdf.MSdfParser;
import mb.spoofax.core.language.LanguageScope;

import javax.inject.Inject;
import javax.inject.Provider;

@LanguageScope
public class MSdfParse extends JSGLR1ParseTaskDef {
    private final javax.inject.Provider<MSdfParser> parserProvider;

    @Inject public MSdfParse(Provider<MSdfParser> parserProvider) {
        this.parserProvider = parserProvider;
    }

    @Override public String getId() {
        return MSdfParse.class.getCanonicalName();
    }

    @Override
    protected JSGLR1ParseResult parse(String text) throws InterruptedException {
        final MSdfParser parser = parserProvider.get();
        return parser.parse(text, "MSDFStart");
    }
}
