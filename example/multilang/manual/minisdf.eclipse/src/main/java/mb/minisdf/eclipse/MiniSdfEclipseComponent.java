package mb.minisdf.eclipse;

import dagger.Component;
import mb.minisdf.spoofax.MiniSdfComponent;
import mb.minisdf.spoofax.MiniSdfModule;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.statix.multilang.MultiLangComponent;

@LanguageScope
@Component(
    modules = {MiniSdfModule.class, MiniSdfEclipseModule.class},
    dependencies = {PlatformComponent.class, MultiLangComponent.class}
)
public interface MiniSdfEclipseComponent extends EclipseLanguageComponent, MiniSdfComponent {
    MiniSdfEditorTracker getEditorTracker();
}
