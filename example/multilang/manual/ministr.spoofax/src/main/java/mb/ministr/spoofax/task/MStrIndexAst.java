package mb.ministr.spoofax.task;

import mb.common.result.Result;
import mb.jsglr.common.ResourceKeyAttachment;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.nabl2.terms.stratego.StrategoTermIndices;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.stratego.common.StrategoRuntimeBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;

public class MStrIndexAst implements TaskDef<ResourceKey, Result<IStrategoTerm, JSGLR1ParseException>> {

    private final MStrParse parse;
    private final ITermFactory tf;

    @Inject public MStrIndexAst(MStrParse parse, StrategoRuntimeBuilder strategoRuntimeBuilder) {
        this.parse = parse;
        this.tf = strategoRuntimeBuilder.build().getTermFactory();
    }

    @Override public String getId() {
        return MStrIndexAst.class.getSimpleName();
    }

    @Override
    public Result<IStrategoTerm, JSGLR1ParseException> exec(ExecContext context, ResourceKey resourceKey) throws Exception {
        // TODO: use result types
        return context.require(parse.createRecoverableAstSupplier(resourceKey))
            .map(ast -> {
                ResourceKeyAttachment.setResourceKey(ast, resourceKey);
                return StrategoTermIndices.index(ast, resourceKey.toString(), tf);
            });
    }
}

