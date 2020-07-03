package mb.ministr.eclipse;

import mb.ministr.spoofax.MiniStrModule;
import mb.pie.api.ExecException;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.util.StatusUtil;
import mb.statix.multilang.eclipse.MultiLangPlugin;
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

public class MiniStrPlugin extends AbstractUIPlugin {
    public static final String pluginId = "ministr.eclipse";

    private static final AtomicReference<MiniStrEclipseComponent> component = new AtomicReference<>();
    private static volatile boolean started = false;

    public synchronized static MiniStrEclipseComponent getComponent() {
        if(!started) {
            throw new RuntimeException("Cannot access MiniStrComponent; MiniStrPlugin has not been started yet, or has been stopped");
        }
        return component.updateAndGet(component -> {
            if(component == null) {
                // Lazy initialize to prevent synchronization issues with MultiLang extension point initialization
                component = DaggerMiniStrEclipseComponent
                    .builder()
                    .platformComponent(SpoofaxPlugin.getComponent())
                    .multiLangComponent(MultiLangPlugin.getComponent())
                    .miniStrModule(new MiniStrModule())
                    .miniStrEclipseModule(new MiniStrEclipseModule())
                    .build();
                component.getEditorTracker().register();
            }
            return component;
        });
    }

    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        started = true;

        new WorkspaceJob("MiniStr startup") {
            @Override public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                try {
                    SpoofaxPlugin.getComponent().getPieRunner().startup(getComponent(), monitor);
                } catch(IOException | ExecException | InterruptedException e) {
                    throw new CoreException(StatusUtil.error("MiniStr startup job failed unexpectedly", e));
                }
                return StatusUtil.success();
            }
        }.schedule();
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        component.set(null);
        started = false;
    }
}
