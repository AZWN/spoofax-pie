package mb.signature;

import mb.statix.multilang.metadata.SpecFragmentId;
import mb.statix.multilang.metadata.spec.ImmutableSpecConfig;
import mb.statix.multilang.metadata.spec.SpecConfig;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;

import java.util.HashMap;
import java.util.Map;

public class ModuleSpecFactory {

    public static SpecFragmentId getSpecId() {
        return new SpecFragmentId("mb.module");
    }

    public static SpecConfig getSpecConfig(ITermFactory termFactory) {
        return ImmutableSpecConfig.builder()
            .rootPackage(ModuleClassloaderResources.createDefinitionDir(ModuleClassloaderResources.createClassLoaderResourceRegistry())
                .appendRelativePath("src-gen/statix"))
            .termFactory(termFactory)
            .addRootModules("module-interface/modules")
            .build();
    }

    public static SpecConfig getSpecConfig() {
        return getSpecConfig(new ImploderOriginTermFactory(new TermFactory()));
    }

    public static Map<SpecFragmentId, SpecConfig> getSpecConfigs() {
        final HashMap<SpecFragmentId, SpecConfig> result = new HashMap<>();
        result.put(ModuleSpecFactory.getSpecId(), ModuleSpecFactory.getSpecConfig());
        result.putAll(SigSpecFactory.getSpecConfigs());
        return result;
    }
}
