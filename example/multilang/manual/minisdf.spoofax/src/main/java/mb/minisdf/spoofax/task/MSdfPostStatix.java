package mb.minisdf.spoofax.task;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.AstStrategoTransformTaskDef;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;

public class MSdfPostStatix extends AstStrategoTransformTaskDef {
    @Inject public MSdfPostStatix(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider, "post-analyze");
    }

    @Override public String getId() {
        return MSdfPostStatix.class.getCanonicalName();
    }

    @Override
    public Result<IStrategoTerm, ?> exec(ExecContext context, Supplier<? extends Result<IStrategoTerm, ?>> supplier) {
        try {
            return super.exec(context, supplier);
        } catch(IOException e) {
            return Result.ofErr(e);
        }
    }
}
