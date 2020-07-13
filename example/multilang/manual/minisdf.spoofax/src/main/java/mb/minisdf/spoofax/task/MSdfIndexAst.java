package mb.minisdf.spoofax.task;

import mb.jsglr1.common.JSGLR1ParseException;
import mb.statix.multilang.pie.transform.SmlIndexAst;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

public class MSdfIndexAst extends SmlIndexAst<JSGLR1ParseException> {

    @Inject public MSdfIndexAst(Provider<StrategoRuntime> strategoRuntimeProvider, MSdfParse parse) {
        super(strategoRuntimeProvider, parse.createAstFunction());
    }

    @Override public String getId() {
        return MSdfIndexAst.class.getSimpleName();
    }
}
