package mb.minisdf.spoofax.task;

import mb.common.style.Styling;
import mb.common.token.Token;
import mb.minisdf.MSdfStyler;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;

public class MSdfStyle implements TaskDef<Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>>, @Nullable Styling> {
    private final MSdfStyler styler;

    @Inject public MSdfStyle(MSdfStyler styler) {
        this.styler = styler;
    }

    @Override public String getId() {
        return MSdfStyle.class.getCanonicalName();
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
