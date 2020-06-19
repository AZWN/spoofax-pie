package mb.ministr.eclipse;

import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.editor.EditorTracker;

import javax.inject.Inject;

@LanguageScope
public class MiniStrEditorTracker extends EditorTracker {
    @Inject public MiniStrEditorTracker(EclipseIdentifiers eclipseIdentifiers) {
        super(eclipseIdentifiers);
    }
}
