package mb.minisdf.spoofax;

import dagger.Component;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.platform.PlatformComponent;
import mb.statix.multilang.metadata.LanguageMetadataProvider;
import mb.statix.multilang.MultiLangComponent;

@LanguageScope
@Component(
    modules = MiniSdfModule.class,
    dependencies = {PlatformComponent.class, MultiLangComponent.class}
)
public interface MiniSdfComponent extends LanguageComponent, LanguageMetadataProvider {
    @Override MiniSdfInstance getLanguageInstance();
}
