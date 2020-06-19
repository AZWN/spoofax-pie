package mb.multilang.cli;

import com.google.common.collect.Lists;
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
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.runtime.PieBuilderImpl;
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
import mb.statix.multilang.MultiLangComponent;
import mb.statix.multilang.spec.SpecUtils;
import mb.stratego.common.StrategoRuntime;
import org.metaborg.util.iterators.Iterables2;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        SpoofaxCliComponent platformComponent = DaggerSpoofaxCliComponent.builder()
            .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
            .loggerFactoryModule(new LoggerFactoryModule(new SLF4JLoggerFactory()))
            .resourceRegistriesModule(new ResourceRegistriesModule())
            .build();

        MultiLangComponent multiLangComponent = DaggerMultiLangComponent.builder()
            .platformComponent(platformComponent)
            .build();

        MiniSdfComponent miniSdfComponent = DaggerMiniSdfComponent.builder()
            .platformComponent(platformComponent)
            .multiLangComponent(multiLangComponent)
            .miniSdfModule(new MiniSdfModule())
            .build();

        MiniStrComponent miniStrComponent = DaggerMiniStrComponent.builder()
            .platformComponent(platformComponent)
            .multiLangComponent(multiLangComponent)
            .miniStrModule(new MiniStrModule())
            .build();

        MiniSdfInstance miniSdf = miniSdfComponent.getLanguageInstance();
        MiniStrInstance miniStr = miniStrComponent.getLanguageInstance();
        LanguageId miniSdfLanguageId = new LanguageId("mini-sdf");
        LanguageId miniStrLanguageId = new LanguageId("mini-str");

        ResourceKeyString miniSdfSpecPath = ResourceKeyString.of("mb/minisdf/src-gen/statix");
        ClassLoaderResource miniSdfSpec = MSdfClassloaderResources
            .createClassLoaderResourceRegistry()
            .getResource(miniSdfSpecPath);

        LanguageMetadata miniSdfMetadata = ImmutableLanguageMetadata.builder()
            .resourcesSupplier((exec, projectDir) -> {
                HierarchicalResource res = exec.getHierarchicalResource(projectDir);
                try {
                    return res
                        .walk(
                            new PathResourceWalker(new NoHiddenPathMatcher()),
                            new PathResourceMatcher(new ExtensionsPathMatcher(miniSdf.getFileExtensions().asUnmodifiable())))
                        .map(HierarchicalResource::getKey)
                        .collect(Collectors.toCollection(HashSet::new));
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            })
            .astFunction(miniSdf.preStatix().createFunction()
                .mapInput((exec, key) -> miniSdf.indexAst().createSupplier(key)))
            .postTransform(miniSdf.postStatix().createFunction())
            .languageId(new LanguageId("mb.minisdf"))
            .statixSpec(SpecUtils.loadSpec(miniSdfSpec, "mini-sdf/mini-sdf-typing", miniSdf.termFactory()))
            .fileConstraint("mini-sdf/mini-sdf-typing!msdfProgramOK")
            .projectConstraint("mini-sdf/mini-sdf-typing!msdfProjectOK")
            .build();

        ResourceKeyString miniStrSpecPath = ResourceKeyString.of("mb/ministr/src-gen/statix");
        ClassLoaderResource miniStrSpec = MSdfClassloaderResources
            .createClassLoaderResourceRegistry()
            .getResource(miniStrSpecPath);

        LanguageMetadata miniStrMetadata = ImmutableLanguageMetadata.builder()
            .resourcesSupplier((exec, projectDir) -> {
                HierarchicalResource res = exec.getHierarchicalResource(projectDir);
                try {
                    return res
                        .walk(
                            new PathResourceWalker(new NoHiddenPathMatcher()),
                            new PathResourceMatcher(new ExtensionsPathMatcher(miniStr.getFileExtensions().asUnmodifiable())))
                        .map(HierarchicalResource::getKey)
                        .collect(Collectors.toCollection(HashSet::new));
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            })
            .astFunction(miniStr.preStatix().createFunction()
                .mapInput((exec, key) -> miniStr.indexAst().createSupplier(key)))
            .postTransform(miniStr.postStatix().createFunction())
            .languageId(new LanguageId("mb.ministr"))
            .statixSpec(SpecUtils.loadSpec(miniStrSpec, "mini-str/mini-str-typing", miniStr.termFactory()))
            .fileConstraint("mini-str/mini-str-typing!mstrProgramOK")
            .projectConstraint("mini-str/mini-str-typing!mstrProjectOK")
            .build();

        ContextConfig config = new ContextConfig();
        config.setLanguages(Lists.newArrayList(miniSdfLanguageId.getId(), miniStrLanguageId.getId()));

        AnalysisContextService analysisContextService = multiLangComponent.getAnalysisContextService();
        analysisContextService.registerLanguageLoader(miniSdfLanguageId, () -> miniSdfMetadata);
        analysisContextService.registerLanguageLoader(miniStrLanguageId, () -> miniStrMetadata);
        analysisContextService.registerContextLanguageProvider(new ContextId("mini-sdf-str"),
            () -> Iterables2.singleton(config));
        analysisContextService.initializeService();

        final MSdfParse mSdfParse = new MSdfParse(miniSdfComponent.getParser());
        final StrategoRuntime sdfRuntime = miniSdfComponent.getStrategoRuntime();

        Pie mergedPie = miniStrComponent.getPie()
            .createChildBuilder()
            .withTaskDefs(new MapTaskDefs(
                mSdfParse,
                new MSdfPreStatix(sdfRuntime),
                new MSdfPostStatix(sdfRuntime)
            ))
            .build();

        final SpoofaxCli cmd = platformComponent.getSpoofaxCmd();
        final int status = cmd.run(args, mergedPie, "combined", miniSdf, miniStr);
        System.exit(status);
    }
}
