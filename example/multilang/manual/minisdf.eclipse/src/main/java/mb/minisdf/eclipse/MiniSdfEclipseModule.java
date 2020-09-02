package mb.minisdf.eclipse;

import dagger.Module;
import dagger.Provides;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.job.LockRule;
import mb.spoofax.eclipse.job.ReadLockRule;
import mb.statix.multilang.MultiLang;

import javax.inject.Named;

@Module
public class MiniSdfEclipseModule {
    @Provides @LanguageScope
    EclipseIdentifiers provideEclipseIdentifiers() {
        return new MiniSdfEclipseIdentifiers();
    }

    @Provides @Named("StartupWriteLock") @LanguageScope
    static LockRule provideStartupWriteLockRule(@MultiLang LockRule startUpWriteLockRule) {
        return startUpWriteLockRule;
    }

    @Provides @LanguageScope
    static ReadLockRule provideStartupReadLockRule(@Named("StartupWriteLock") LockRule writeLock) { return new ReadLockRule(writeLock, "Tiger startup read lock"); }

}
