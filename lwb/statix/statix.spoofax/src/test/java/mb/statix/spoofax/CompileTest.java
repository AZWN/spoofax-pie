package mb.statix.spoofax;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import mb.statix.spoofax.task.StatixCompile;
import org.junit.jupiter.api.Test;
import org.spoofax.terms.util.TermUtils;

import static org.junit.jupiter.api.Assertions.*;

class CompileTest extends TestBase {
    @Test void testCompile() throws Exception {
        final FSResource root = createDir(rootDirectory, "stx");
        final FSResource mainFile = createTextFile(root, "" +
                "module main\n" +
                "imports program\n" +
                "rules\n" +
                "  projectOk : scope\n" +
                "  projectOk(s).\n" +
                "\n" +
                "  fileOk : scope * Program\n" +
                "  fileOk(s, p) :-\n" +
                "    programOk(s, p).\n",
            "main.stx");
        final FSResource programFile = createTextFile(root, "" +
                "module program\n" +
                "signature\n" +
                "  sorts Program constructors\n" +
                "    Program : Program\n" +
                "rules\n" +
                "  programOk : scope * Program\n" +
                "  programOk(s, Program()).\n",
            "program.stx");

        try(final MixedSession session = newSession()) {
            final Result<StatixCompile.Output, ?> mainResult = session.require(compile.createTask(new StatixCompile.Input(root.getPath(), mainFile.getPath())));
            assertTrue(mainResult.isOk());
            final StatixCompile.Output mainOutput = mainResult.unwrap();
            assertEquals("src-gen/statix/main.spec.aterm", mainOutput.relativeOutputPath);
            assertTrue(TermUtils.isAppl(mainOutput.spec, "FileSpec", 6));

            final Result<StatixCompile.Output, ?> programResult = session.require(compile.createTask(new StatixCompile.Input(root.getPath(), programFile.getPath())));
            assertTrue(programResult.isOk());
            final StatixCompile.Output programOutput = programResult.unwrap();
            assertEquals("src-gen/statix/program.spec.aterm", programOutput.relativeOutputPath);
            assertTrue(TermUtils.isAppl(programOutput.spec, "FileSpec", 6));
        }
    }
}
