package mb.calc.spoofax;

import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import mb.stratego.common.StrategoRuntime;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class PrettyPrintTest extends TestBase {
    @Test void testParsePrettyPrint() throws Exception {
        final String text = "1 + 2 * 3;";
        final FSResource resource = createTextFile(text, "test.calc");
        try(final MixedSession session = newSession()) {
            final Result<JSGLR1ParseOutput, JSGLR1ParseException> result = session.require(languageComponent.getCalcParse().createTask(resourceStringSupplier(resource)));
            assertTrue(result.isOk());
            final IStrategoTerm ast = result.unwrap().ast;
            final StrategoRuntime strategoRuntime = languageComponent.getStrategoRuntimeProvider().get();
            final IStrategoTerm term = strategoRuntime.invoke("pp-calc-string", ast);
            assertTrue(isString(term));
            final String str = toJavaString(term);
            assertEquals(text, str);
        }
    }
}
