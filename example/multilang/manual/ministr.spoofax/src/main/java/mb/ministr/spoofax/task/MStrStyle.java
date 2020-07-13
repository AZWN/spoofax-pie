package mb.ministr.spoofax.task;

import mb.common.option.Option;
import mb.common.style.Styling;
import mb.common.token.Token;
import mb.jsglr.common.JSGLRTokens;
import mb.ministr.MStrStyler;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;

public class MStrStyle implements TaskDef<Supplier<Option<JSGLRTokens>>, Option<Styling>>  {
    private final MStrStyler styler;

    @Inject public MStrStyle(MStrStyler styler) {
        this.styler = styler;
    }

    @Override public String getId() {
        return MStrStyle.class.getCanonicalName();
    }

    @Override public Option<Styling> exec(ExecContext context, Supplier<Option<JSGLRTokens>> tokensSupplier) throws IOException {
        return context.require(tokensSupplier).map(t -> styler.style(t.tokens));
    }
}