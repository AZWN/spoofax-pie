package mb.multilang;

import com.google.common.collect.ListMultimap;
import mb.resource.ResourceKeyString;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.statix.multilang.metadata.spec.SpecFragment;
import mb.statix.multilang.metadata.spec.SpecLoadException;
import mb.statix.multilang.metadata.spec.SpecUtils;
import mb.statix.spec.Rule;
import mb.statix.spec.Spec;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CombinedSpecLoadTest {

    private final ITermFactory termFactory = new TermFactory();

    @Disabled("Need to implement dependent module resolution first")
    @Test public void testLoadCombinedSpec() throws SpecLoadException {
        SpecFragment miniSdfSpec = loadSpec("mb/minisdf/src-gen/statix", "mini-sdf/mini-sdf-typing");
        SpecFragment miniStrSpec = loadSpec("mb/ministr/src-gen/statix", "mini-str/mini-str-typing");

        Spec combinedSpec = SpecUtils.mergeSpecs(miniSdfSpec.toSpec(), miniStrSpec.toSpec()).unwrap();

        ListMultimap<String, Rule> overlappingRules = combinedSpec.rules().getAllEquivalentRules();
        assertTrue(overlappingRules.isEmpty());
    }

    private SpecFragment loadSpec(String pkg, String rootModule) throws SpecLoadException {
        ClassLoaderResourceRegistry registry = new ClassLoaderResourceRegistry(CombinedSpecLoadTest.class.getClassLoader());
        ClassLoaderResource specRoot = registry.getResource(ResourceKeyString.of(pkg));
        return SpecUtils.loadSpec(specRoot, rootModule, termFactory);
    }
}
