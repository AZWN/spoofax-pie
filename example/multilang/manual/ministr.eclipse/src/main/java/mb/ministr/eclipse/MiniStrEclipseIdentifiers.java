package mb.ministr.eclipse;

import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.eclipse.EclipseIdentifiers;

@LanguageScope
public class MiniStrEclipseIdentifiers implements EclipseIdentifiers {

    @Override public String getPlugin() {
        return MiniStrPlugin.pluginId;
    }

    @Override public String getContext() {
        return "ministr.eclipse.context";
    }

    @Override public String getDocumentProvider() {
        return "ministr.eclipse.documentprovider";
    }

    @Override public String getEditor() {
        return "ministr.eclipse.editor";
    }

    @Override public String getNature() {
        return MiniStrNature.id;
    }

    @Override public String getProjectBuilder() {
        return MiniStrProjectBuilder.id;
    }


    @Override public String getBaseMarker() {
        return "ministr.eclipse.marker";
    }

    @Override public String getInfoMarker() {
        return "ministr.eclipse.marker.info";
    }

    @Override public String getWarningMarker() {
        return "ministr.eclipse.marker.warning";
    }

    @Override public String getErrorMarker() {
        return "ministr.eclipse.marker.error";
    }


    @Override public String getAddNatureCommand() {
        return "ministr.eclipse.nature.add";
    }

    @Override public String getRemoveNatureCommand() {
        return "ministr.eclipse.nature.remove";
    }

    @Override public String getObserveCommand() {
        return "ministr.eclipse.observe";
    }

    @Override public String getUnobserveCommand() {
        return "ministr.eclipse.unobserve";
    }

    @Override public String getRunCommand() {
        return "ministr.eclipse.command";
    }


    @Override public String getResourceContextMenu() {
        return "ministr.eclipse.menu.resource.context";
    }

    @Override public String getEditorContextMenu() {
        return "ministr.eclipse.menu.editor.context";
    }

    @Override public String getMainMenu() {
        return "ministr.eclipse.menu.main";
    }

    @Override public String getMainMenuDynamic() {
        return "ministr.eclipse.menu.dynamic";
    }
}
