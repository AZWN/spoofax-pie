package mb.minisdf.spoofax.task;

import mb.common.option.Option;
import mb.common.style.Styling;
import mb.common.token.Token;
import mb.jsglr.common.JSGLRTokens;
import mb.minisdf.MSdfStyler;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;

public class MSdfStyle implements TaskDef<Supplier<Option<JSGLRTokens>>, Option<Styling>> {
    private final MSdfStyler styler;

    @Inject public MSdfStyle(MSdfStyler styler) {
        this.styler = styler;
    }

    @Override public String getId() {
        return MSdfStyle.class.getCanonicalName();
    }

    @Override public Option<Styling> exec(ExecContext context, Supplier<Option<JSGLRTokens>> tokensSupplier) throws IOException {
        return context.require(tokensSupplier).map(t -> styler.style(t.tokens));
    }
}
