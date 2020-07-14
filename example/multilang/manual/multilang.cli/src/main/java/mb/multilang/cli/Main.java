package mb.multilang.cli;

import com.google.common.collect.Lists;
import mb.common.util.SetView;
import mb.log.slf4j.SLF4JLoggerFactory;
import mb.minisdf.spoofax.DaggerMiniSdfComponent;
import mb.minisdf.spoofax.MiniSdfComponent;
import mb.minisdf.spoofax.MiniSdfModule;
import mb.ministr.spoofax.DaggerMiniStrComponent;
import mb.ministr.spoofax.MiniStrComponent;
import mb.ministr.spoofax.MiniStrModule;
import mb.pie.api.Pie;
import mb.pie.runtime.PieBuilderImpl;
import mb.spoofax.cli.DaggerSpoofaxCliComponent;
import mb.spoofax.cli.SpoofaxCli;
import mb.spoofax.cli.SpoofaxCliComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.core.platform.ResourceRegistriesModule;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.ContextConfig;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.DaggerMultiLangComponent;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.MultiLangComponent;
import mb.statix.multilang.MultiLangModule;

import java.util.function.Supplier;

public class Main {
    public static MiniSdfComponent miniSdfComponent;
    public static MiniStrComponent miniStrComponent;

    public static void main(String[] args) throws MultiLangAnalysisException {
        SpoofaxCliComponent platformComponent = DaggerSpoofaxCliComponent.builder()
            .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
            .loggerFactoryModule(new LoggerFactoryModule(new SLF4JLoggerFactory()))
            .resourceRegistriesModule(new ResourceRegistriesModule())
            .build();

        LanguageId miniSdfLanguageId = new LanguageId("mb.minisdf");
        LanguageId miniStrLanguageId = new LanguageId("mb.ministr");
        ContextId contextId = new ContextId("mini-sdf-str");

        Supplier<AnalysisContextService> analysisContextService = () -> AnalysisContextService.builder()
            .putContextConfigurations(contextId, SetView.of(miniSdfLanguageId, miniStrLanguageId).asUnmodifiable())
            .putDefaultLanguageContexts(miniSdfLanguageId, contextId)
            .putDefaultLanguageContexts(miniStrLanguageId, contextId)
            .putLanguageMetadataSuppliers(miniSdfLanguageId, miniSdfComponent::getLanguageMetadata)
            .putLanguageMetadataSuppliers(miniStrLanguageId, miniStrComponent::getLanguageMetadata)
            .platformPie(platformComponent.getPie())
            .build();

        MultiLangComponent multiLangComponent = DaggerMultiLangComponent.builder()
            .platformComponent(platformComponent)
            .multiLangModule(new MultiLangModule(analysisContextService))
            .build();

        miniSdfComponent = DaggerMiniSdfComponent.builder()
            .platformComponent(platformComponent)
            .multiLangComponent(multiLangComponent)
            .miniSdfModule(new MiniSdfModule())
            .build();

        miniStrComponent = DaggerMiniStrComponent.builder()
            .platformComponent(platformComponent)
            .multiLangComponent(multiLangComponent)
            .miniStrModule(new MiniStrModule())
            .build();

        ContextConfig config = new ContextConfig();
        config.setLanguages(Lists.newArrayList(miniSdfLanguageId, miniStrLanguageId));

        final SpoofaxCli cmd = platformComponent.getSpoofaxCmd();
        Pie mergedPie = analysisContextService.get().buildPieForLanguages(SetView.of(miniSdfLanguageId, miniStrLanguageId).asUnmodifiable());

        final int status = cmd.run(args, mergedPie, "combined", miniSdfComponent.getLanguageInstance(), miniStrComponent.getLanguageInstance());
        System.exit(status);
    }
}
