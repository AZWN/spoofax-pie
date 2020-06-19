package mb.minisdf.spoofax.task;

import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class MSdfPostStatix implements TaskDef<Supplier<IStrategoTerm>, IStrategoTerm> {
    private final StrategoRuntime strategoRuntime;

    @Inject public MSdfPostStatix(StrategoRuntime strategoRuntime) {
        this.strategoRuntime = strategoRuntime;
    }


    @Override public String getId() {
        return MSdfPostStatix.class.getCanonicalName();
    }

    @Override
    public IStrategoTerm exec(ExecContext context, Supplier<IStrategoTerm> input) throws Exception {
        return strategoRuntime.invoke("post-analyze", context.require(input));
    }
}
