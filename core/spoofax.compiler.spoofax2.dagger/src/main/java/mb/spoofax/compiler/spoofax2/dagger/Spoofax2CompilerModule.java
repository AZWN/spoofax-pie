package mb.spoofax.compiler.spoofax2.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.spoofax.compiler.dagger.*;
import mb.spoofax.compiler.spoofax2.language.Spoofax2LanguageProjectCompiler;
import mb.spoofax.compiler.spoofax2.language.Spoofax2MultilangAnalyzerLanguageCompiler;
import mb.spoofax.compiler.spoofax2.language.Spoofax2ParserLanguageCompiler;
import mb.spoofax.compiler.spoofax2.language.Spoofax2StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.spoofax2.language.Spoofax2StylerLanguageCompiler;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Module(includes = SpoofaxCompilerModule.class)
public class Spoofax2CompilerModule {
    @Provides @Singleton @ElementsIntoSet static Set<TaskDef<?, ?>> provideTaskDefsSet(
        Spoofax2LanguageProjectCompiler languageProjectCompiler,
        Spoofax2ParserLanguageCompiler parserLanguageCompiler,
        Spoofax2StylerLanguageCompiler stylerLanguageCompiler,
        Spoofax2StrategoRuntimeLanguageCompiler strategoRuntimeLanguageCompiler,
        Spoofax2MultilangAnalyzerLanguageCompiler multilangAnalyzerLanguageCompiler
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(languageProjectCompiler);
        taskDefs.add(parserLanguageCompiler);
        taskDefs.add(stylerLanguageCompiler);
        taskDefs.add(strategoRuntimeLanguageCompiler);
        taskDefs.add(multilangAnalyzerLanguageCompiler);
        return taskDefs;
    }
}