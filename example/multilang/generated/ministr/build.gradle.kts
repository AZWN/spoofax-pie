import mb.spoofax.compiler.spoofaxcore.CompleterCompiler
import mb.spoofax.compiler.spoofaxcore.LanguageProjectCompiler
import mb.spoofax.compiler.spoofaxcore.MultilangAnalyzerCompiler
import mb.spoofax.compiler.spoofaxcore.ParserCompiler
import mb.spoofax.compiler.spoofaxcore.StrategoRuntimeCompiler
import mb.spoofax.compiler.spoofaxcore.StylerCompiler
import mb.spoofax.compiler.util.GradleDependency

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.language")
}

dependencies {
  testImplementation("org.metaborg:log.backend.noop")
  testCompileOnly("org.checkerframework:checker-qual-android")
}

spoofaxLanguageProject {
  settings.set(mb.spoofax.compiler.gradle.spoofaxcore.LanguageProjectSettings(
    shared = mb.spoofax.compiler.spoofaxcore.Shared.builder()
      .name("MiniStr")
      .fileExtensions(listOf("mstr"))
      .defaultBasePackageId("mb.ministr"),

    parser = ParserCompiler.LanguageProjectInput.builder()
      .startSymbol("MSTRStart"),
    styler = StylerCompiler.LanguageProjectInput.builder(),
    completer = CompleterCompiler.LanguageProjectInput.builder(),
    strategoRuntime = StrategoRuntimeCompiler.LanguageProjectInput.builder()
      .enableNaBL2(false)
      .enableStatix(false)
      .copyClasses(false)
      .copyJavaStrategyClasses(false)
      .copyCTree(true),
    multilangAnalyzer = MultilangAnalyzerCompiler.LanguageProjectInput.builder(),

    builder = LanguageProjectCompiler.Input.builder()
      .languageSpecificationDependency(GradleDependency.module("$group:ministr.spoofaxcore:$version"))
  ))
}
