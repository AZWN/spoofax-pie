package mb.sdf3.spoofax;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.sdf3.spoofax.task.Sdf3ToCompletionColorer;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class ToCompletionColorerTest extends TestBase {
    @Test void testTask() throws Exception {
        final TextResource resource = createTextResource("module nested/a context-free syntax A = <A>", "a.sdf3");
        final Sdf3ToCompletionColorer taskDef = languageComponent.getSdf3ToCompletionColorer();
        try(final MixedSession session = newSession()) {
            final Result<IStrategoTerm, ?> result = session.require(taskDef.createTask(desugarSupplier(resource)));
            assertTrue(result.isOk());
            final IStrategoTerm output = result.unwrap();
            log.info("{}", output);
            assertNotNull(output);
            assertTrue(isAppl(output, "Module"));
            assertTrue(isStringAt(output, 0, "completion/colorer/nested/a-cc-esv"));
        }
    }
}
