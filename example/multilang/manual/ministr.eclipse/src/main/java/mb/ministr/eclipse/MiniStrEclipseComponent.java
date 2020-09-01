package mb.ministr.eclipse;

import dagger.Component;
import mb.ministr.spoofax.MiniStrComponent;
import mb.ministr.spoofax.MiniStrModule;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.statix.multilang.MultiLangComponent;
import mb.statix.multilang.eclipse.MultiLangEclipseComponent;

@LanguageScope
@Component(
    modules = {MiniStrModule.class, MiniStrEclipseModule.class},
    dependencies = {PlatformComponent.class, MultiLangEclipseComponent.class}
)
public interface MiniStrEclipseComponent extends EclipseLanguageComponent, MiniStrComponent {
    MiniStrEditorTracker getEditorTracker();
}
