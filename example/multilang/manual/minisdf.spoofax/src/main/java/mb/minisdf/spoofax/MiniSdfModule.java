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
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.tasks.SmlAnalyzeProject;
import mb.statix.multilang.tasks.SmlBuildMessages;
import mb.statix.multilang.tasks.SmlInstantiateGlobalScope;
import mb.statix.multilang.tasks.SmlPartialSolveFile;
import mb.statix.multilang.tasks.SmlPartialSolveProject;
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

    @Provides @LanguageScope @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        MSdfParse parse,
        MSdfStyle style,
        MSdfComplete complete,
        MSdfPreStatix preStatix,
        MSdfPostStatix postStatix,
        MSdfIndexAst index,

        SmlBuildMessages analyze,
        SmlAnalyzeProject analyzeProject,
        SmlPartialSolveFile partialSolveFile,
        SmlPartialSolveProject partialSolveProject,
        SmlInstantiateGlobalScope instantiateGlobalScope,

        MSdfAnalyzeProject analyzeMSdfProject
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();

        taskDefs.add(parse);
        taskDefs.add(style);
        taskDefs.add(complete);
        taskDefs.add(preStatix);
        taskDefs.add(postStatix);
        taskDefs.add(index);

        taskDefs.add(analyze);
        taskDefs.add(analyzeProject);
        taskDefs.add(partialSolveFile);
        taskDefs.add(partialSolveProject);
        taskDefs.add(instantiateGlobalScope);

        taskDefs.add(analyzeMSdfProject);

        return taskDefs;
    }

    @Provides @LanguageScope
    TaskDefs provideTaskDefs(Set<TaskDef<?, ?>> taskDefs) {
        return new MapTaskDefs(taskDefs);
    }

    @Provides @LanguageScope
    static Pie providePie(AnalysisContextService analysisContext) {
        // Always return PIE instance from analysis context, to get optimal incrementality
        // For tasks that are dependent on/dependents of analysis tasks.
        return analysisContext.getAnalysisContext(new ContextId("mini-sdf-str")).createPieForContext();
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