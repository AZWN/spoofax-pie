package mb.minisdf.eclipse;

import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.eclipse.EclipseIdentifiers;

@LanguageScope
public class MiniSdfEclipseIdentifiers implements EclipseIdentifiers {

    @Override public String getPlugin() {
        return MiniSdfPlugin.pluginId;
    }

    @Override public String getContext() {
        return "minisdf.eclipse.context";
    }

    @Override public String getDocumentProvider() {
        return "minisdf.eclipse.documentprovider";
    }

    @Override public String getEditor() {
        return "minisdf.eclipse.editor";
    }

    @Override public String getNature() {
        return MiniSdfNature.id;
    }

    @Override public String getProjectBuilder() {
        return MiniSdfProjectBuilder.id;
    }


    @Override public String getBaseMarker() {
        return "minisdf.eclipse.marker";
    }

    @Override public String getInfoMarker() {
        return "minisdf.eclipse.marker.info";
    }

    @Override public String getWarningMarker() {
        return "minisdf.eclipse.marker.warning";
    }

    @Override public String getErrorMarker() {
        return "minisdf.eclipse.marker.error";
    }


    @Override public String getAddNatureCommand() {
        return "minisdf.eclipse.nature.add";
    }

    @Override public String getRemoveNatureCommand() {
        return "minisdf.eclipse.nature.remove";
    }

    @Override public String getObserveCommand() {
        return "minisdf.eclipse.observe";
    }

    @Override public String getUnobserveCommand() {
        return "minisdf.eclipse.unobserve";
    }

    @Override public String getRunCommand() {
        return "minisdf.eclipse.command";
    }


    @Override public String getResourceContextMenu() {
        return "minisdf.eclipse.menu.resource.context";
    }

    @Override public String getEditorContextMenu() {
        return "minisdf.eclipse.menu.editor.context";
    }

    @Override public String getMainMenu() {
        return "minisdf.eclipse.menu.main";
    }

    @Override public String getMainMenuDynamic() {
        return "minisdf.eclipse.menu.dynamic";
    }
}
