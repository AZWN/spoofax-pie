package mb.calc.spoofax;

import mb.stratego.common.StrategoRuntime;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class StrategoRuntimeTest extends TestBase {
    @Test void testParseTask() throws Exception {
        final StrategoRuntime strategoRuntime = languageComponent.getStrategoRuntimeProvider().get();
        final IStrategoTerm term = strategoRuntime.invoke("main", strategoRuntime.getTermFactory().makeTuple());
        assertTrue(isString(term, "Hello, world!"));
    }
}