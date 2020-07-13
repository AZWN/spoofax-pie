package mb.ministr.spoofax;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.log.api.LoggerFactory;
import mb.ministr.MStrClassloaderResources;
import mb.ministr.MStrParser;
import mb.ministr.MStrParserFactory;
import mb.ministr.MStrStrategoRuntimeBuilderFactory;
import mb.ministr.MStrStyler;
import mb.ministr.MStrStylerFactory;
import mb.ministr.spoofax.command.MStrShowAnalyzedAstCommand;
import mb.ministr.spoofax.task.MStrAnalyzeProject;
import mb.ministr.spoofax.task.MStrComplete;
import mb.ministr.spoofax.task.MStrIndexAst;
import mb.ministr.spoofax.task.MStrParse;
import mb.ministr.spoofax.task.MStrPostStatix;
import mb.ministr.spoofax.task.MStrPreStatix;
import mb.ministr.spoofax.task.MStrSmlCheck;
import mb.ministr.spoofax.task.MStrStyle;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceService;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.resource.hierarchical.match.path.NoHiddenPathMatcher;
import mb.resource.hierarchical.walk.PathResourceWalker;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.pie.PieProvider;
import mb.spoofax.core.platform.Platform;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.ImmutableLanguageMetadata;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadata;
import mb.statix.multilang.MultiLang;
import mb.statix.multilang.SharedPieProvider;
import mb.statix.multilang.pie.SmlAnalyzeProject;
import mb.statix.multilang.pie.config.SmlBuildContextConfiguration;
import mb.statix.multilang.pie.SmlBuildMessages;
import mb.statix.multilang.pie.SmlBuildSpec;
import mb.statix.multilang.pie.SmlInstantiateGlobalScope;
import mb.statix.multilang.pie.SmlPartialSolveFile;
import mb.statix.multilang.pie.SmlPartialSolveProject;
import mb.statix.multilang.pie.config.SmlReadConfigYaml;
import mb.statix.multilang.spec.SpecBuilder;
import mb.statix.multilang.spec.SpecLoadException;
import mb.statix.multilang.spec.SpecUtils;
import mb.statix.multilang.utils.MetadataUtils;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Module
public class MiniStrModule {
    @Provides @LanguageScope
    static ClassLoaderResourceRegistry provideClassLoaderResourceRegistry() {
        return MStrClassloaderResources.createClassLoaderResourceRegistry();
    }

    @Provides @LanguageScope
    static ResourceService provideResourceRegistry(@Platform ResourceService resourceService, ClassLoaderResourceRegistry classLoaderResourceRegistry) {
        return resourceService.createChild(classLoaderResourceRegistry);
    }

    @Provides @Named("definition-dir") @LanguageScope
    static ClassLoaderResource provideDefinitionDir(ClassLoaderResourceRegistry registry) {
        return MStrClassloaderResources.createDefinitionDir(registry);
    }

    @Provides @Named("definition-dir") @LanguageScope
    static HierarchicalResource provideDefinitionDirAsHierarchicalResource(@Named("definition-dir") ClassLoaderResource definitionDir) {
        return definitionDir;
    }

    @Provides @LanguageScope
    static MStrParserFactory provideParserFactory(@Named("definition-dir") HierarchicalResource definitionDir) {
        return new MStrParserFactory(definitionDir);
    }

    @Provides /* Unscoped: parser has state, so create a new parser every call. */
    static MStrParser provideParser(MStrParserFactory parserFactory) {
        return parserFactory.create();
    }

    @Provides @LanguageScope
    static MStrStylerFactory provideStylerFactory(LoggerFactory loggerFactory, @Named("definition-dir") HierarchicalResource definitionDir) {
        return new MStrStylerFactory(loggerFactory, definitionDir);
    }

    @Provides @LanguageScope
    static MStrStyler provideStyler(MStrStylerFactory stylerFactory) {
        return stylerFactory.create();
    }

    @Provides @LanguageScope
    static MStrStrategoRuntimeBuilderFactory provideStrategoRuntimeBuilderFactory(LoggerFactory loggerFactory, ResourceService resourceService, @Named("definition-dir") HierarchicalResource definitionDir) {
        return new MStrStrategoRuntimeBuilderFactory(loggerFactory, resourceService, definitionDir);
    }

    @Provides @LanguageScope
    static StrategoRuntimeBuilder provideStrategoRuntimeBuilder(MStrStrategoRuntimeBuilderFactory factory) {
        return factory.create();
    }

    @Provides @LanguageScope @Named("prototype")
    static StrategoRuntime providePrototypeStrategoRuntime(StrategoRuntimeBuilder builder) {
        return builder.build();
    }

    @Provides @LanguageScope
    static ITermFactory provideTermFactory(@Named("prototype") StrategoRuntime strategoRuntime) {
        return strategoRuntime.getTermFactory();
    }

    @Provides /* Unscoped: new session every call. */
    static StrategoRuntime provideStrategoRuntime(StrategoRuntimeBuilder builder, @Named("prototype") StrategoRuntime prototype) {
        return builder.buildFromPrototype(prototype);
    }
    // Inject sml tasks into correct scope
    @Provides @LanguageScope
    static SmlAnalyzeProject provideAnalyzeProject(@MultiLang SmlAnalyzeProject analyzeProject) {
        return analyzeProject;
    }

    @Provides @LanguageScope
    static SmlBuildContextConfiguration provideBuildContextConfiguration(
        @MultiLang SmlBuildContextConfiguration buildContextConfiguration
    ) {
        return buildContextConfiguration;
    }

    @Provides @LanguageScope
    static SmlBuildMessages provideBuildMessages(@MultiLang SmlBuildMessages buildMessages) {
        return buildMessages;
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
        MStrParse parse,
        MStrStyle style,
        MStrComplete complete,
        MStrPreStatix preStatix,
        MStrPostStatix postStatix,
        MStrIndexAst index,
        MStrSmlCheck check,
        MStrAnalyzeProject analyzeMStrProject,

        SmlBuildContextConfiguration buildContextConfiguration,
        SmlReadConfigYaml readConfigYaml,

        SmlAnalyzeProject analyzeProject,
        SmlBuildMessages buildMessages,
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
        taskDefs.add(analyzeMStrProject);

        taskDefs.add(buildContextConfiguration);
        taskDefs.add(readConfigYaml);

        taskDefs.add(analyzeProject);
        taskDefs.add(buildMessages);
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

    @Provides @LanguageScope
    static Pie providePie(@Platform Pie pie, TaskDefs taskDefs, ResourceService resourceService) {
        return pie.createChildBuilder().withTaskDefs(taskDefs).withResourceService(resourceService).build();
    }

    @Provides @LanguageScope
    static LanguageId provideLanguageId() {
        return new LanguageId("mb.ministr");
    }

    @Provides @LanguageScope
    static PieProvider providePieProvider(SharedPieProvider pieProvider) {
        return pieProvider;
    }

    @Provides @LanguageScope @ElementsIntoSet
    static Set<CommandDef<?>> provideCommandDefsSet(MStrShowAnalyzedAstCommand showAnalyzedAstCommand) {
        final HashSet<CommandDef<?>> commandDefs = new HashSet<>();
        commandDefs.add(showAnalyzedAstCommand);
        return commandDefs;
    }

    @Provides @LanguageScope @ElementsIntoSet
    static Set<AutoCommandRequest<?>> provideAutoCommandRequestsSet() {
        return new HashSet<>();
    }

    @Provides @LanguageScope
    static LanguageInstance provideLanguageInstance(MiniStrInstance miniStrInstance) {
        return miniStrInstance;
    }

    @Provides @LanguageScope
    static AnalysisContextService getAnalysisContextService(@MultiLang AnalysisContextService analysisContextService) {
        return analysisContextService;
    }

    @Provides @LanguageScope
    static LanguageMetadata getLanguageMetadata(
        @Named("prototype") StrategoRuntime strategoRuntime,
        MStrPreStatix preStatix,
        MStrPostStatix postStatix,
        MStrIndexAst indexAst,
        Pie languagePie
    ) {
        ITermFactory termFactory = strategoRuntime.getTermFactory();

        return ImmutableLanguageMetadata.builder()
            .resourcesSupplier(MetadataUtils.resourcesSupplierForExtensions("mstr"))
            .astFunction(preStatix.createFunction().mapInput(indexAst::createSupplier))
            .postTransform(postStatix.createFunction().mapInput((exec, i) -> i)) // mapInput needed for typing
            .languageId(new LanguageId("mb.ministr"))
            .languagePie(languagePie)
            .termFactory(termFactory)
            .statixSpec(MetadataUtils.loadSpec(MStrClassloaderResources.defaultDefinitionDir(), termFactory, "mini-str/mini-str-typing"))
            .fileConstraint("mini-str/mini-str-typing!mstrProgramOK")
            .projectConstraint("mini-str/mini-str-typing!mstrProjectOK")
            .build();
    }
}
