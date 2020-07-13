package mb.minisdf.spoofax.task;

import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.pie.TaskUtils;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class MSdfPreStatix implements TaskDef<Supplier<Result<IStrategoTerm, JSGLR1ParseException>>, Result<IStrategoTerm, MultiLangAnalysisException>> {
    private final StrategoRuntime strategoRuntime;

    @Inject public MSdfPreStatix(StrategoRuntime strategoRuntime) {
        this.strategoRuntime = strategoRuntime;
    }


    @Override public String getId() {
        return MSdfPreStatix.class.getCanonicalName();
    }

    @Override
    public Result<IStrategoTerm, MultiLangAnalysisException> exec(ExecContext context, Supplier<Result<IStrategoTerm, JSGLR1ParseException>> input) throws Exception {
        return TaskUtils.executeWrapped(() -> context.require(input)
            .mapErr(MultiLangAnalysisException::new)
            .flatMap(TaskUtils.executeWrapped(ast -> Result.ofOk(strategoRuntime.invoke("pre-analyze", ast)))));
    }
}
