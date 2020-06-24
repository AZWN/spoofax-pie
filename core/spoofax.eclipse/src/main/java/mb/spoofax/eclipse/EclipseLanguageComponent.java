package mb.spoofax.eclipse;

import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.eclipse.job.LockRule;

@LanguageScope
public interface EclipseLanguageComponent extends LanguageComponent {
    EclipseIdentifiers getEclipseIdentifiers();

    LockRule startupLockRule();
}
