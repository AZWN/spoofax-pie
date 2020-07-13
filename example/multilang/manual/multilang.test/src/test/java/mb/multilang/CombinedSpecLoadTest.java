package mb.multilang;

import com.google.common.collect.ListMultimap;
import mb.resource.ResourceKeyString;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.statix.multilang.spec.SpecBuilder;
import mb.statix.multilang.spec.SpecLoadException;
import mb.statix.multilang.spec.SpecUtils;
import mb.statix.spec.Rule;
import mb.statix.spec.Spec;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CombinedSpecLoadTest {

    private final ITermFactory termFactory = new TermFactory();

    @Test public void testLoadCombinedSpec() throws IOException, SpecLoadException {
        SpecBuilder miniSdfSpec = loadSpec("mb/minisdf/src-gen/statix", "mini-sdf/mini-sdf-typing");
        SpecBuilder miniStrSpec = loadSpec("mb/ministr/src-gen/statix", "mini-str/mini-str-typing");

        Spec combinedSpec = miniSdfSpec.extend().addAllModules(miniStrSpec.modules()).build().toSpec();

        ListMultimap<String, Rule> overlappingRules = combinedSpec.rules().getAllEquivalentRules();
        assertTrue(overlappingRules.isEmpty());
    }

    private SpecBuilder loadSpec(String pkg, String rootModule) throws IOException {
        ClassLoaderResourceRegistry registry = new ClassLoaderResourceRegistry(CombinedSpecLoadTest.class.getClassLoader());
        ClassLoaderResource specRoot = registry.getResource(ResourceKeyString.of(pkg));
        return SpecUtils.loadSpec(specRoot, rootModule, termFactory);
    }
}
