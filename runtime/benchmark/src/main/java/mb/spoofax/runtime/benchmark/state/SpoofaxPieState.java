package mb.spoofax.runtime.benchmark.state;

import com.google.inject.Injector;
import mb.log.LogModule;
import mb.log.Logger;
import mb.pie.runtime.builtin.PieBuiltinModule;
import mb.spoofax.runtime.benchmark.SpoofaxCoreModule;
import mb.spoofax.runtime.impl.SpoofaxImplModule;
import mb.spoofax.runtime.impl.legacy.StaticSpoofaxCoreFacade;
import mb.spoofax.runtime.model.SpoofaxEx;
import mb.spoofax.runtime.model.SpoofaxFacade;
import mb.spoofax.runtime.model.SpoofaxModule;
import mb.spoofax.runtime.model.StaticSpoofaxFacade;
import mb.spoofax.runtime.pie.SpoofaxPieModule;
import mb.spoofax.runtime.pie.SpoofaxPipeline;
import mb.spoofax.runtime.pie.generated.PieBuilderModule_spoofax;
import mb.vfs.VFSModule;
import mb.vfs.path.PathSrv;
import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.meta.core.SpoofaxExtensionModule;
import org.metaborg.spoofax.meta.core.SpoofaxMeta;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.LoggerFactory;


@State(Scope.Benchmark)
public class SpoofaxPieState {
    public final SpoofaxFacade spoofaxFacade;
    public final Spoofax spoofaxCoreFacade;
    public final SpoofaxMeta spoofaxCoreMetaFacade;
    public final Injector injector;
    public final Logger logger;
    public final PathSrv pathSrv;
    public final SpoofaxPipeline spoofaxPipeline;


    public SpoofaxPieState() {
        try {
            spoofaxFacade =
                new SpoofaxFacade(new SpoofaxModule(), new LogModule(LoggerFactory.getLogger("root")), new VFSModule(),
                    new SpoofaxImplModule(), new SpoofaxPieModule(), new PieBuiltinModule(),
                    new PieBuilderModule_spoofax());
            StaticSpoofaxFacade.init(spoofaxFacade);
            spoofaxCoreFacade = new Spoofax(new SpoofaxCoreModule(), new SpoofaxExtensionModule());
            spoofaxCoreMetaFacade = new SpoofaxMeta(spoofaxCoreFacade);
            StaticSpoofaxCoreFacade.init(spoofaxCoreMetaFacade);
            injector = spoofaxFacade.injector;
            logger = injector.getInstance(Logger.class);
            pathSrv = injector.getInstance(PathSrv.class);
            spoofaxPipeline = SpoofaxPipeline.INSTANCE;
        } catch(SpoofaxEx | MetaborgException e) {
            throw new RuntimeException(e);
        }
    }
}
