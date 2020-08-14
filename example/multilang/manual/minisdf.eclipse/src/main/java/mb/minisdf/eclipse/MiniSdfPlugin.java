package mb.minisdf.eclipse;

import mb.minisdf.spoofax.MiniSdfModule;
import mb.pie.api.ExecException;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.util.StatusUtil;
import mb.statix.multilang.eclipse.MultiLangPlugin;
import mb.statix.multilang.eclipse.UpdateAnalysisConfigChangeListener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class MiniSdfPlugin extends AbstractUIPlugin {
    public static final String pluginId = "minisdf.eclipse";

    private static @Nullable MiniSdfEclipseComponent component;
    private static @Nullable UpdateAnalysisConfigChangeListener configListener;

    public static MiniSdfEclipseComponent getComponent() {
        if(component == null) {
            throw new RuntimeException(
                "Cannot access MiniSdfComponent; MiniSdfPlugin has not been started yet, or has been stopped");
        }
        return component;
    }

    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        component = DaggerMiniSdfEclipseComponent
            .builder()
            .platformComponent(SpoofaxPlugin.getComponent())
            .multiLangComponent(MultiLangPlugin.getComponent())
            .miniSdfModule(new MiniSdfModule())
            .miniSdfEclipseModule(new MiniSdfEclipseModule())
            .build();

        component.getEditorTracker().register();
        configListener = new UpdateAnalysisConfigChangeListener(component);
        MultiLangPlugin.getConfigResourceChangeListener().addDelegate(configListener);

        WorkspaceJob startupJob = new WorkspaceJob("MiniSdf startup") {
            @Override public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                try {
                    SpoofaxPlugin.getComponent().getPieRunner().startup(getComponent(), monitor);
                } catch(IOException | ExecException | InterruptedException e) {
                    throw new CoreException(StatusUtil.error("MiniSdf startup job failed unexpectedly", e));
                }
                return StatusUtil.success();
            }
        };
        startupJob.setRule(MultiLangPlugin.getComponent().startUpLockRule());
        startupJob.schedule();
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        if(configListener != null) {
            MultiLangPlugin.getConfigResourceChangeListener().removeDelegate(configListener);
        }
        configListener = null;
        component = null;
    }
}
