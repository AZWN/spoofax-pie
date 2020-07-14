package mb.ministr.spoofax.task;

import mb.jsglr1.common.JSGLR1ParseException;
import mb.statix.multilang.pie.transform.SmlIndexAstTaskDef;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

public class MStrIndexAst extends SmlIndexAstTaskDef<JSGLR1ParseException> {

    @Inject public MStrIndexAst(Provider<StrategoRuntime> strategoRuntimeProvider, MStrParse parse) {
        super(strategoRuntimeProvider, parse.createAstFunction());
    }

    @Override public String getId() {
        return MStrIndexAst.class.getSimpleName();
    }
}

