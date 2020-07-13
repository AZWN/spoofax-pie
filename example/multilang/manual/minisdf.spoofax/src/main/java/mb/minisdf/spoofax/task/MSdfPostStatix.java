package mb.minisdf.spoofax.task;

import mb.common.option.Option;
import mb.common.result.ExceptionalFunction;
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
import java.util.function.Function;

public class MSdfPostStatix implements TaskDef<Supplier<Option<IStrategoTerm>>, Option<IStrategoTerm>> {
    private final StrategoRuntime strategoRuntime;

    @Inject public MSdfPostStatix(StrategoRuntime strategoRuntime) {
        this.strategoRuntime = strategoRuntime;
    }


    @Override public String getId() {
        return MSdfPostStatix.class.getCanonicalName();
    }

    @Override
    public Option<IStrategoTerm> exec(ExecContext context, Supplier<Option<IStrategoTerm>> input) {
        return TaskUtils.executeWrapped(() -> context.require(input)
            .mapOrElse(Result::<IStrategoTerm, MultiLangAnalysisException>ofOk,
                () -> Result.<IStrategoTerm, MultiLangAnalysisException>ofErr(new MultiLangAnalysisException("No ast provided for post transformation")))
            .flatMap(TaskUtils.executeWrapped((IStrategoTerm ast) -> Result.ofOk(strategoRuntime.invoke("post-analyze", ast))))
        ).ok();
    }
}
