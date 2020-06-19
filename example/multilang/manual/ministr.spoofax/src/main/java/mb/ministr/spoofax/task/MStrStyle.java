package mb.ministr.spoofax.task;

import mb.common.style.Styling;
import mb.common.token.Token;
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

public class MStrStyle implements TaskDef<Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>>, @Nullable Styling> {
    private final MStrStyler styler;

    @Inject public MStrStyle(MStrStyler styler) {
        this.styler = styler;
    }

    @Override public String getId() {
        return MStrStyle.class.getCanonicalName();
    }

    @Override public @Nullable Styling exec(
        ExecContext context,
        Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>> tokensSupplier
    ) throws ExecException, IOException, InterruptedException {
        final @Nullable ArrayList<? extends Token<IStrategoTerm>> tokens = context.require(tokensSupplier);
        if(tokens == null) {
            return null;
        } else {
            return styler.style(tokens);
        }
    }
}
