package mb.signature;

import mb.statix.multilang.metadata.SpecFragmentId;
import mb.statix.multilang.metadata.spec.ImmutableSpecConfig;
import mb.statix.multilang.metadata.spec.SpecConfig;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;

import java.util.HashMap;
import java.util.Map;

public class SigSpecFactory {

    public static SpecFragmentId getSpecId() {
        return new SpecFragmentId("mb.signature");
    }

    public static SpecConfig getSpecConfig(ITermFactory termFactory) {
        return ImmutableSpecConfig.builder()
            .rootPackage(SigClassloaderResources.createDefinitionDir(SigClassloaderResources.createClassLoaderResourceRegistry())
                .appendRelativePath("src-gen/statix"))
            .termFactory(termFactory)
            .addRootModules("cons-type-interface/conflicts/sorts", "cons-type-interface/conflicts/constructors")
            .build();
    }

    public static SpecConfig getSpecConfig() {
        return getSpecConfig(new ImploderOriginTermFactory(new TermFactory()));
    }

    public static Map<SpecFragmentId, SpecConfig> getSpecConfigs() {
        final HashMap<SpecFragmentId, SpecConfig> result = new HashMap<>();
        result.put(SigSpecFactory.getSpecId(), SigSpecFactory.getSpecConfig());
        return result;
    }
}
