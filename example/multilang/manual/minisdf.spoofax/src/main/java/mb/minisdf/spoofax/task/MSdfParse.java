package mb.minisdf.spoofax.task;

import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.jsglr1.pie.JSGLR1ParseTaskDef;
import mb.minisdf.MSdfParser;
import mb.spoofax.core.language.LanguageScope;

import javax.inject.Inject;
import javax.inject.Provider;

@LanguageScope
public class MSdfParse extends JSGLR1ParseTaskDef {
    private final Provider<MSdfParser> parserProvider;

    @Inject public MSdfParse(Provider<MSdfParser> parserProvider) {
        this.parserProvider = parserProvider;
    }

    @Override public String getId() {
        return MSdfParse.class.getCanonicalName();
    }

    @Override
    protected Result<JSGLR1ParseOutput, JSGLR1ParseException> parse(String text) throws InterruptedException {
        final MSdfParser parser = parserProvider.get();
        try {
            return Result.ofOk(parser.parse(text, "MSDFStart"));
        } catch(JSGLR1ParseException e) {
            return Result.ofErr(e);
        }
    }
}
