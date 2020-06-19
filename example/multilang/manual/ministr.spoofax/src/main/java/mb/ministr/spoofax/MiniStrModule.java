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
import mb.ministr.spoofax.task.MStrStyle;
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

    @Provides @LanguageScope @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        MStrParse parse,
        MStrStyle style,
        MStrComplete complete,
        MStrPreStatix preStatix,
        MStrPostStatix postStatix,
        MStrIndexAst indexAst,

        SmlBuildMessages analyze,
        SmlAnalyzeProject analyzeProject,
        SmlPartialSolveFile partialSolveFile,
        SmlPartialSolveProject partialSolveProject,
        SmlInstantiateGlobalScope instantiateGlobalScope,

        MStrAnalyzeProject analyzeMStrProject
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();

        taskDefs.add(parse);
        taskDefs.add(style);
        taskDefs.add(complete);
        taskDefs.add(preStatix);
        taskDefs.add(postStatix);
        taskDefs.add(indexAst);

        taskDefs.add(analyze);
        taskDefs.add(analyzeProject);
        taskDefs.add(partialSolveFile);
        taskDefs.add(partialSolveProject);
        taskDefs.add(instantiateGlobalScope);

        taskDefs.add(analyzeMStrProject);

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
}
