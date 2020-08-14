package mb.minisdf;

import mb.signature.SigSpecFactory;
import mb.statix.multilang.metadata.SpecFragmentId;
import mb.statix.multilang.metadata.spec.ImmutableSpecConfig;
import mb.statix.multilang.metadata.spec.SpecConfig;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;

import java.util.HashMap;
import java.util.Map;

public class MSdfSpecFactory {

    public static SpecFragmentId getSpecId() {
        return new SpecFragmentId("mb.minisdf");
    }

    public static SpecConfig getSpecConfig(ITermFactory termFactory) {
        return ImmutableSpecConfig.builder()
            .rootPackage(MSdfClassloaderResources.createDefinitionDir(MSdfClassloaderResources.createClassLoaderResourceRegistry())
                .appendRelativePath("src-gen/statix"))
            .termFactory(termFactory)
            .addRootModules("mini-sdf/mini-sdf-typing")
            .addDependencies(SigSpecFactory.getSpecId())
            .build();
    }

    public static SpecConfig getSpecConfig() {
        return getSpecConfig(new ImploderOriginTermFactory(new TermFactory()));
    }

    public static Map<SpecFragmentId, SpecConfig> getSpecConfigs(ITermFactory termFactory) {
        final HashMap<SpecFragmentId, SpecConfig> result = new HashMap<>();
        result.putAll(SigSpecFactory.getSpecConfigs());
        result.put(getSpecId(), getSpecConfig(termFactory));
        return result;
    }
}
