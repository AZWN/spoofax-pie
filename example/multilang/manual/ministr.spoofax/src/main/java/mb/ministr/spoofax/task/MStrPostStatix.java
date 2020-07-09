package mb.ministr.spoofax.task;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class MStrPostStatix implements TaskDef<Supplier<Option<IStrategoTerm>>, Option<IStrategoTerm>> {
    private final StrategoRuntime strategoRuntime;

    @Inject public MStrPostStatix(StrategoRuntime strategoRuntime) {
        this.strategoRuntime = strategoRuntime;
    }


    @Override public String getId() {
        return MStrPostStatix.class.getCanonicalName();
    }

    @Override
    public Option<IStrategoTerm> exec(ExecContext context, Supplier<Option<IStrategoTerm>> input) throws Exception {
        return context.require(input).map(ast -> {
            try {
                return strategoRuntime.invoke("post-analyze", ast);
            } catch(StrategoException e) {
                // Todo: wrap in result type
                throw new MultiLangAnalysisException(e);
            }
        });
    }
}
