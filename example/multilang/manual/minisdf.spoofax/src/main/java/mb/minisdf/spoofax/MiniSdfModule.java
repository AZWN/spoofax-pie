package mb.minisdf.spoofax;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.log.api.LoggerFactory;
import mb.minisdf.MSdfClassloaderResources;
import mb.minisdf.MSdfParser;
import mb.minisdf.MSdfParserFactory;
import mb.minisdf.MSdfSpecFactory;
import mb.minisdf.MSdfStrategoRuntimeBuilderFactory;
import mb.minisdf.MSdfStyler;
import mb.minisdf.MSdfStylerFactory;
import mb.minisdf.spoofax.command.MSdfShowAnalyzedAstCommand;
import mb.minisdf.spoofax.task.MSdfAnalyzeProject;
import mb.minisdf.spoofax.task.MSdfComplete;
import mb.minisdf.spoofax.task.MSdfIndexAst;
import mb.minisdf.spoofax.task.MSdfParse;
import mb.minisdf.spoofax.task.MSdfPostStatix;
import mb.minisdf.spoofax.task.MSdfPreStatix;
import mb.minisdf.spoofax.task.MSdfSmlCheck;
import mb.minisdf.spoofax.task.MSdfStyle;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import mb.resource.ResourceService;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.platform.Platform;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.metadata.AnalysisContextService;
import mb.statix.multilang.metadata.ContextDataManager;
import mb.statix.multilang.metadata.ContextPieManager;
import mb.statix.multilang.metadata.ImmutableLanguageMetadata;
import mb.statix.multilang.metadata.LanguageId;
import mb.statix.multilang.metadata.LanguageMetadata;
import mb.statix.multilang.metadata.LanguageMetadataManager;
import mb.statix.multilang.MultiLang;
import mb.statix.multilang.metadata.SpecFragmentId;
import mb.statix.multilang.metadata.spec.SpecConfig;
import mb.statix.multilang.pie.SmlBuildSpec;
import mb.statix.multilang.pie.SmlInstantiateGlobalScope;
import mb.statix.multilang.pie.SmlPartialSolveFile;
import mb.statix.multilang.pie.SmlPartialSolveProject;
import mb.statix.multilang.pie.SmlSolveProject;
import mb.statix.multilang.pie.config.SmlBuildContextConfiguration;
import mb.statix.multilang.pie.config.SmlReadConfigYaml;
import mb.statix.multilang.utils.MetadataUtils;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Named;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Module
public class MiniSdfModule {
    @Provides @LanguageScope
    static ClassLoaderResourceRegistry provideClassLoaderResourceRegistry() {
        return MSdfClassloaderResources.createClassLoaderResourceRegistry();
    }

    @Provides @LanguageScope
    static ResourceService provideResourceRegistry(@Platform ResourceService resourceService, ClassLoaderResourceRegistry classLoaderResourceRegistry) {
        return resourceService.createChild(classLoaderResourceRegistry);
    }

    @Provides @Named("definition-dir") @LanguageScope
    static ClassLoaderResource provideDefinitionDir(ClassLoaderResourceRegistry registry) {
        return MSdfClassloaderResources.createDefinitionDir(registry);
    }

    @Provides @Named("definition-dir") @LanguageScope
    static HierarchicalResource provideDefinitionDirAsHierarchicalResource(@Named("definition-dir") ClassLoaderResource definitionDir) {
        return definitionDir;
    }

    @Provides @LanguageScope
    static MSdfParserFactory provideParserFactory(@Named("definition-dir") HierarchicalResource definitionDir) {
        return new MSdfParserFactory(definitionDir);
    }

    @Provides /* Unscoped: parser has state, so create a new parser every call. */
    static MSdfParser provideParser(MSdfParserFactory parserFactory) {
        return parserFactory.create();
    }

    @Provides @LanguageScope
    static MSdfStylerFactory provideStylerFactory(LoggerFactory loggerFactory, @Named("definition-dir") HierarchicalResource definitionDir) {
        return new MSdfStylerFactory(loggerFactory, definitionDir);
    }

    @Provides @LanguageScope
    static MSdfStyler provideStyler(MSdfStylerFactory stylerFactory) {
        return stylerFactory.create();
    }

    @Provides @LanguageScope
    static MSdfStrategoRuntimeBuilderFactory provideStrategoRuntimeBuilderFactory(LoggerFactory loggerFactory, ResourceService resourceService, @Named("definition-dir") HierarchicalResource definitionDir) {
        return new MSdfStrategoRuntimeBuilderFactory(loggerFactory, resourceService, definitionDir);
    }

    @Provides @LanguageScope
    static StrategoRuntimeBuilder provideStrategoRuntimeBuilder(MSdfStrategoRuntimeBuilderFactory factory) {
        return factory.create();
    }

    @Provides @LanguageScope @Named("prototype")
    static StrategoRuntime providePrototypeStrategoRuntime(StrategoRuntimeBuilder builder) {
        return builder.build();
    }

    @Provides /* Unscoped: new session every call. */
    static StrategoRuntime provideStrategoRuntime(StrategoRuntimeBuilder builder, @Named("prototype") StrategoRuntime prototype) {
        return builder.buildFromPrototype(prototype);
    }

    // Inject sml tasks into correct scope
    @Provides @LanguageScope
    static SmlSolveProject provideAnalyzeProject(@MultiLang SmlSolveProject analyzeProject) {
        return analyzeProject;
    }

    @Provides @LanguageScope
    static SmlBuildContextConfiguration provideBuildContextConfiguration(
        @MultiLang SmlBuildContextConfiguration buildContextConfiguration
    ) {
        return buildContextConfiguration;
    }

    @Provides @LanguageScope
    static SmlBuildSpec provideBuildSpec(@MultiLang SmlBuildSpec buildSpec) {
        return buildSpec;
    }

    @Provides @LanguageScope
    static SmlPartialSolveFile providePartialSolveFile(@MultiLang SmlPartialSolveFile partialSolveFile) {
        return partialSolveFile;
    }

    @Provides @LanguageScope
    static SmlPartialSolveProject providePartialSolveProject(@MultiLang SmlPartialSolveProject partialSolveProject) {
        return partialSolveProject;
    }

    @Provides @LanguageScope
    static SmlInstantiateGlobalScope provideInstantiateGlobalScope(@MultiLang SmlInstantiateGlobalScope instantiateGlobalScope) {
        return instantiateGlobalScope;
    }

    @Provides @LanguageScope
    static SmlReadConfigYaml provideReadConfigYaml(@MultiLang SmlReadConfigYaml readConfigYaml) {
        return readConfigYaml;
    }

    @Provides @LanguageScope @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        MSdfParse parse,
        MSdfStyle style,
        MSdfComplete complete,
        MSdfPreStatix preStatix,
        MSdfPostStatix postStatix,
        MSdfIndexAst index,
        MSdfSmlCheck check,
        MSdfAnalyzeProject analyzeMSdfProject,

        SmlBuildContextConfiguration buildContextConfiguration,
        SmlReadConfigYaml readConfigYaml,

        SmlSolveProject analyzeProject,
        SmlBuildSpec buildSpec,
        SmlPartialSolveProject partialSolveProject,
        SmlPartialSolveFile partialSolveFile,
        SmlInstantiateGlobalScope instantiateGlobalScope
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();

        taskDefs.add(parse);
        taskDefs.add(style);
        taskDefs.add(complete);
        taskDefs.add(preStatix);
        taskDefs.add(postStatix);
        taskDefs.add(index);

        taskDefs.add(check);
        taskDefs.add(analyzeMSdfProject);

        taskDefs.add(buildContextConfiguration);
        taskDefs.add(readConfigYaml);

        taskDefs.add(analyzeProject);
        taskDefs.add(buildSpec);
        taskDefs.add(partialSolveFile);
        taskDefs.add(partialSolveProject);
        taskDefs.add(instantiateGlobalScope);

        return taskDefs;
    }

    @Provides @LanguageScope
    TaskDefs provideTaskDefs(Set<TaskDef<?, ?>> taskDefs) {
        return new MapTaskDefs(taskDefs);
    }

    @Provides @LanguageScope @Named("prototype")
    static Pie providePrototypePie(@Platform Pie pie, TaskDefs taskDefs, ResourceService resourceService) {
        return pie.createChildBuilder().withTaskDefs(taskDefs).withResourceService(resourceService).build();
    }

    @Provides @LanguageScope
    static LanguageId provideLanguageId() {
        return new LanguageId("mb.minisdf");
    }

    @Provides @LanguageScope
    static Pie providePieProvider(ContextPieManager pieManager) {
        try {
            return pieManager.buildPieForContext();
        } catch(MultiLangAnalysisException e) {
            throw new RuntimeException("Cannot build shared Pie", e);
        }
    }

    @Provides @LanguageScope @ElementsIntoSet
    static Set<CommandDef<?>> provideCommandDefsSet(MSdfShowAnalyzedAstCommand showAnalyzedAstCommand) {
        final HashSet<CommandDef<?>> commandDefs = new HashSet<>();
        commandDefs.add(showAnalyzedAstCommand);
        return commandDefs;
    }

    @Provides @LanguageScope @ElementsIntoSet
    static Set<AutoCommandRequest<?>> provideAutoCommandRequestsSet() {
        return new HashSet<>();
    }

    @Provides @LanguageScope
    static LanguageInstance provideLanguageInstance(MiniSdfInstance miniSdfInstance) {
        return miniSdfInstance;
    }

    @Provides @LanguageScope
    static AnalysisContextService getAnalysisContextService(@MultiLang AnalysisContextService analysisContextService) {
        return analysisContextService;
    }

    @Provides @LanguageScope
    static LanguageMetadataManager provideLanguageMetadataManager(@MultiLang AnalysisContextService analysisContextService) {
        return analysisContextService;
    }

    @Provides @LanguageScope
    static ContextPieManager provideContextPieManager(@MultiLang AnalysisContextService analysisContextService) {
        return analysisContextService;
    }

    @Provides @LanguageScope
    static ContextDataManager provideContextDataManager(@MultiLang AnalysisContextService analysisContextService) {
        return analysisContextService;
    }

    @Provides @LanguageScope
    static LanguageMetadata getLanguageMetadata(
        @Named("prototype") StrategoRuntime strategoRuntime,
        @Named("definition-dir") ClassLoaderResource definitionDir,
        MSdfPreStatix preStatix,
        MSdfPostStatix postStatix,
        MSdfIndexAst indexAst,
        @Named("prototype") Pie languagePie
    ) {
        ITermFactory termFactory = strategoRuntime.getTermFactory();
        return ImmutableLanguageMetadata.builder()
            .resourcesSupplier(MetadataUtils.resourcesSupplierForExtensions("msdf"))
            .astFunction(preStatix.createFunction().mapInput(indexAst::createSupplier))
            .postTransform(postStatix.createFunction())
            .languageId(new LanguageId("mb.minisdf"))
            .languagePie(languagePie)
            .termFactory(termFactory)
            .fileConstraint("mini-sdf/mini-sdf-typing!msdfProgramOK")
            .projectConstraint("mini-sdf/mini-sdf-typing!msdfProjectOK")
            .build();
    }

    @Provides @LanguageScope
    static Map<SpecFragmentId, SpecConfig> getSpecConfigs(@Named("prototype") StrategoRuntime strategoRuntime) {
        return MSdfSpecFactory.getSpecConfigs(strategoRuntime.getTermFactory());
    }
}
