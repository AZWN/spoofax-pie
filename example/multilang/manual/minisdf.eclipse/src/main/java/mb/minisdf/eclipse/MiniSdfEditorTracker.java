package mb.minisdf.eclipse;

import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.editor.EditorTracker;

import javax.inject.Inject;

@LanguageScope
public class MiniSdfEditorTracker extends EditorTracker {
    @Inject public MiniSdfEditorTracker(EclipseIdentifiers eclipseIdentifiers) {
        super(eclipseIdentifiers);
    }
}
