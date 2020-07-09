package mb.ministr.eclipse;

import dagger.Module;
import dagger.Provides;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.job.LockRule;
import mb.spoofax.eclipse.job.ReadLockRule;

import javax.inject.Named;

@Module
public class MiniStrEclipseModule {
    @Provides @LanguageScope
    EclipseIdentifiers provideEclipseIdentifiers() {
        return new mb.ministr.eclipse.MiniStrEclipseIdentifiers();
    }

    @Provides @Named("StartupWriteLock") @LanguageScope
    static LockRule provideStartupWriteLockRule() { return new LockRule("Tiger startup write lock"); }

    @Provides @LanguageScope
    static ReadLockRule provideStartupReadLockRule(@Named("StartupWriteLock") LockRule writeLock) { return new ReadLockRule(writeLock, "Tiger startup read lock"); }

}
