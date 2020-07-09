package mb.ministr.spoofax.task;

import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class MStrPreStatix implements TaskDef<Supplier<Result<IStrategoTerm, JSGLR1ParseException>>, Result<IStrategoTerm, JSGLR1ParseException>> {
    private final StrategoRuntime strategoRuntime;

    @Inject public MStrPreStatix(StrategoRuntime strategoRuntime) {
        this.strategoRuntime = strategoRuntime;
    }


    @Override public String getId() {
        return MStrPreStatix.class.getCanonicalName();
    }

    @Override
    public Result<IStrategoTerm, JSGLR1ParseException> exec(ExecContext context, Supplier<Result<IStrategoTerm, JSGLR1ParseException>> input) throws Exception {
        return context.require(input)
            .map(ast -> {
                try {
                    return strategoRuntime.invoke("pre-analyze", ast);
                } catch(StrategoException e) {
                    // Todo: wrap in result type
                    throw new MultiLangAnalysisException(e);
                }
            });
    }
}
