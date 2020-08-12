package mb.ministr.spoofax;

import dagger.Component;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.platform.PlatformComponent;
import mb.statix.multilang.metadata.LanguageMetadataProvider;
import mb.statix.multilang.MultiLangComponent;

@LanguageScope
@Component(
    modules = MiniStrModule.class,
    dependencies = {PlatformComponent.class, MultiLangComponent.class}
)
public interface MiniStrComponent extends LanguageComponent, LanguageMetadataProvider {
    @Override MiniStrInstance getLanguageInstance();
}
