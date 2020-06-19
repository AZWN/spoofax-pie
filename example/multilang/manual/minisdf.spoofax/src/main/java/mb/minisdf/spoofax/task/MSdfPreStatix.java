package mb.minisdf.spoofax.task;

import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class MSdfPreStatix implements TaskDef<Supplier<IStrategoTerm>, IStrategoTerm> {
    private final StrategoRuntime strategoRuntime;

    @Inject public MSdfPreStatix(StrategoRuntime strategoRuntime) {
        this.strategoRuntime = strategoRuntime;
    }


    @Override public String getId() {
        return MSdfPreStatix.class.getCanonicalName();
    }

    @Override
    public IStrategoTerm exec(ExecContext context, Supplier<IStrategoTerm> input) throws Exception {
        IStrategoTerm ast = context.require(input);
        return strategoRuntime.invoke("pre-analyze", ast);
    }
}
