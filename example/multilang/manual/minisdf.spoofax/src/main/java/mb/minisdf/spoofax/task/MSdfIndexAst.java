package mb.minisdf.spoofax.task;

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
import java.io.IOException;

public class MSdfIndexAst implements TaskDef<ResourceKey, Result<IStrategoTerm, JSGLR1ParseException>> {

    private final MSdfParse parse;
    private final ITermFactory tf;

    @Inject public MSdfIndexAst(MSdfParse parse, StrategoRuntimeBuilder strategoRuntimeBuilder) {
        this.parse = parse;
        this.tf = strategoRuntimeBuilder.build().getTermFactory();
    }

    @Override public String getId() {
        return MSdfIndexAst.class.getSimpleName();
    }

    @Override
    public Result<IStrategoTerm, JSGLR1ParseException> exec(ExecContext context, ResourceKey resourceKey) throws IOException {
        // TODO: use result types
        return context.require(parse.createRecoverableAstSupplier(resourceKey))
            .map(ast -> {
                ResourceKeyAttachment.setResourceKey(ast, resourceKey);
                return StrategoTermIndices.index(ast, resourceKey.toString(), tf);
            });
    }
}

