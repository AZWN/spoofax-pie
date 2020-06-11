package mb.statix.multilang.spec;

import mb.resource.DefaultResourceKeyString;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.statix.spec.RuleSet;
import mb.statix.spec.Spec;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class SpecLoaderTest {

    private final ClassLoaderResourceRegistry resourceRegistry = new ClassLoaderResourceRegistry(SpecLoaderTest.class.getClassLoader());
    private final ITermFactory termFactory = new TermFactory();

    @Test void loadEmptySpec() throws IOException {
        final ClassLoaderResource statixRoot = resourceRegistry
            .getResource(new DefaultResourceKeyString("mb/statix/multilang/empty"));

        Spec spec = SpecUtils
            .loadSpec(statixRoot, "empty", termFactory)
            .toSpec();
        assertNotNull(spec);
    }

    @Test void loadRegularSpec() throws IOException {
        final ClassLoaderResource statixRoot = resourceRegistry
            .getResource(new DefaultResourceKeyString("mb/statix/multilang/base"));
        Spec spec = SpecUtils
            .loadSpec(statixRoot, "base", termFactory)
            .toSpec();
        assertNotNull(spec);

        assertEquals(0, spec.edgeLabels().size());
        assertEquals(1, spec.relationLabels().size()); // Decl()
        RuleSet rules = spec.rules();
        assertEquals(1, rules.getRules("base!ok").size());
        assertEquals(1, rules.getRules("imported!rule").size());
    }

    @Test void loadCompatibleSpecs() throws IOException {
        final ClassLoaderResource root1 = resourceRegistry
            .getResource(new DefaultResourceKeyString("mb/statix/multilang/base"));
        SpecBuilder specBuilder1 = SpecUtils
            .loadSpec(root1, "base", termFactory);

        final ClassLoaderResource root2 = resourceRegistry
            .getResource(new DefaultResourceKeyString("mb/statix/multilang/compatible"));
        SpecBuilder specBuilder2 = SpecUtils
            .loadSpec(root2, "compatible", termFactory);

        Spec spec = specBuilder1.withModules(specBuilder2.modules()).toSpec();

        assertEquals(0, spec.edgeLabels().size());
        assertEquals(1, spec.relationLabels().size()); // Decl()
        RuleSet rules = spec.rules();
        assertEquals(1, rules.getRules("root!ok").size());
        assertEquals(1, rules.getRules("imported!rule").size());
    }

    @Test void loadIncompatibleSpecs() throws IOException {
        final ClassLoaderResource root1 = resourceRegistry
            .getResource(new DefaultResourceKeyString("mb/statix/multilang/base"));
        SpecBuilder specBuilder1 = SpecUtils
            .loadSpec(root1, "base", termFactory);

        final ClassLoaderResource root2 = resourceRegistry
            .getResource(new DefaultResourceKeyString("mb/statix/multilang/incompatible"));
        SpecBuilder specBuilder2 = SpecUtils
            .loadSpec(root2, "incompatible", termFactory);
        assertThrows(SpecLoadException.class, () -> specBuilder1.extend().addAllModules(specBuilder2.modules()).build());
    }
}
