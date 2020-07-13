package mb.multilang.cli;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import mb.common.result.Result;
import mb.common.util.SetView;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.log.slf4j.SLF4JLoggerFactory;
import mb.minisdf.MSdfClassloaderResources;
import mb.minisdf.spoofax.DaggerMiniSdfComponent;
import mb.minisdf.spoofax.MiniSdfComponent;
import mb.minisdf.spoofax.MiniSdfInstance;
import mb.minisdf.spoofax.MiniSdfModule;
import mb.minisdf.spoofax.task.MSdfParse;
import mb.minisdf.spoofax.task.MSdfPostStatix;
import mb.minisdf.spoofax.task.MSdfPreStatix;
import mb.ministr.spoofax.DaggerMiniStrComponent;
import mb.ministr.spoofax.MiniStrComponent;
import mb.ministr.spoofax.MiniStrInstance;
import mb.ministr.spoofax.MiniStrModule;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.resource.hierarchical.match.path.NoHiddenPathMatcher;
import mb.resource.hierarchical.walk.PathResourceWalker;
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
import mb.statix.multilang.ImmutableLanguageMetadata;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadata;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.MultiLangComponent;
import mb.statix.multilang.MultiLangModule;
import mb.statix.multilang.spec.SpecBuilder;
import mb.statix.multilang.spec.SpecUtils;
import mb.stratego.common.StrategoRuntime;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

public class Main {
    public static MiniSdfComponent miniSdfComponent;
    public static MiniStrComponent miniStrComponent;

    public static void main(String[] args) throws MultiLangAnalysisException {
        SpoofaxCliComponent platformComponent = DaggerSpoofaxCliComponent.builder()
            .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
            .loggerFactoryModule(new LoggerFactoryModule(new SLF4JLoggerFactory()))
            .resourceRegistriesModule(new ResourceRegistriesModule())
            .build();

        LanguageId miniSdfLanguageId = new LanguageId("mini-sdf");
        LanguageId miniStrLanguageId = new LanguageId("mini-str");
        ContextId contextId = new ContextId("mini-sdf-str");

        AnalysisContextService analysisContextService = AnalysisContextService.builder()
            .putContextConfigurations(contextId, SetView.of(miniSdfLanguageId, miniStrLanguageId).asUnmodifiable())
            .putDefaultLanguageContexts(miniSdfLanguageId, contextId)
            .putDefaultLanguageContexts(miniStrLanguageId, contextId)
            .putLanguageMetadataSuppliers(miniSdfLanguageId, miniSdfComponent::getLanguageMetadata)
            .putLanguageMetadataSuppliers(miniStrLanguageId, miniStrComponent::getLanguageMetadata)
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
        Pie mergedPie = analysisContextService.buildPieForLanguages(SetView.of(miniSdfLanguageId, miniStrLanguageId).asUnmodifiable());

        final int status = cmd.run(args, mergedPie, "combined", miniSdfComponent.getLanguageInstance(), miniStrComponent.getLanguageInstance());
        System.exit(status);
    }
}
