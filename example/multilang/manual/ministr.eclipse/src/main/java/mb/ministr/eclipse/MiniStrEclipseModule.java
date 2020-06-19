package mb.ministr.eclipse;

import dagger.Module;
import dagger.Provides;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.eclipse.EclipseIdentifiers;

@Module
public class MiniStrEclipseModule {
    @Provides @LanguageScope
    EclipseIdentifiers provideEclipseIdentifiers() {
        return new mb.ministr.eclipse.MiniStrEclipseIdentifiers();
    }
}
