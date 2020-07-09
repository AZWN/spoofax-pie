package mb.ministr.spoofax.task;

import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
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
    protected Result<JSGLR1ParseOutput, JSGLR1ParseException> parse(String text) throws InterruptedException {
        final MStrParser parser = parserProvider.get();
        try {
            return Result.ofOk(parser.parse(text, "MSTRStart"));
        } catch(JSGLR1ParseException e) {
            return Result.ofErr(e);
        }
    }
}
