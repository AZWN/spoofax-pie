package mb.ministr;

import mb.signature.ModuleSpecFactory;
import mb.signature.SigSpecFactory;
import mb.statix.multilang.metadata.SpecFragmentId;
import mb.statix.multilang.metadata.spec.ImmutableSpecConfig;
import mb.statix.multilang.metadata.spec.SpecConfig;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;

import java.util.HashMap;
import java.util.Map;

public class MStrSpecFactory {

    public static SpecFragmentId getSpecId() {
        return new SpecFragmentId("mb.ministr");
    }

    public static SpecConfig getSpecConfig(ITermFactory termFactory) {
        return ImmutableSpecConfig.builder()
            .rootPackage(MStrClassloaderResources.createDefinitionDir(MStrClassloaderResources.createClassLoaderResourceRegistry())
                .appendRelativePath("src-gen/statix"))
            .termFactory(termFactory)
            .addRootModules("mini-str/mini-str-typing")
            .addDependencies(SigSpecFactory.getSpecId())
            .build();
    }

    public static SpecConfig getSpecConfig() {
        return getSpecConfig(new ImploderOriginTermFactory(new TermFactory()));
    }

    public static Map<SpecFragmentId, SpecConfig> getSpecConfigs(ITermFactory termFactory) {
        final HashMap<SpecFragmentId, SpecConfig> result = new HashMap<>(ModuleSpecFactory.getSpecConfigs());
        result.put(getSpecId(), getSpecConfig(termFactory));
        return result;
    }
}
