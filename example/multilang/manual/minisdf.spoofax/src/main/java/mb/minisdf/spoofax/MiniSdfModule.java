package mb.minisdf.spoofax;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.log.api.LoggerFactory;
import mb.minisdf.MSdfClassloaderResources;
import mb.minisdf.MSdfParser;
import mb.minisdf.MSdfParserFactory;
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
import mb.spoofax.core.pie.PieProvider;
import mb.spoofax.core.platform.Platform;
import mb.statix.multilang.LanguageId;
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
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Named;
import java.util.HashSet;
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
        taskDefs.add(analyzeMSdfProject);

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
        return new LanguageId("mb.minisdf");
    }

    @Provides @LanguageScope
    static PieProvider providePieProvider(SharedPieProvider pieProvider) {
        return pieProvider;
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
}
