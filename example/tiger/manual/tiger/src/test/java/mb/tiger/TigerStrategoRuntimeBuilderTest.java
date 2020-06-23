package mb.tiger;

import mb.common.result.MessagesException;
import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.stratego.common.StrategoException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.junit.jupiter.api.Assertions.*;

class TigerStrategoRuntimeBuilderTest extends TigerTestBase {
    @Test void parseUnparse() throws InterruptedException, StrategoException {
        final String str = "1 + 2";
        final Result<JSGLR1ParseOutput, MessagesException> parsed = parser.parse(str, "Module");
        assertTrue(parsed.isOk());
        final @Nullable IStrategoTerm unparsedTerm = strategoRuntime.invoke("pp-Tiger-string", parsed.unwrapUnchecked().ast);
        assertNotNull(unparsedTerm);
        final IStrategoString unparsedStringTerm = (IStrategoString)unparsedTerm;
        final String unparsed = unparsedStringTerm.stringValue();
        assertEquals(str, unparsed);
    }
}
