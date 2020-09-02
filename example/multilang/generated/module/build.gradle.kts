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
  api(project(":signature"))
  testImplementation("org.metaborg:log.backend.noop")
  testCompileOnly("org.checkerframework:checker-qual-android")
}

spoofaxLanguageProject {
  statixDependencies.set(listOf(project(":signature")))
  settings.set(mb.spoofax.compiler.gradle.spoofaxcore.LanguageProjectSettings(
    shared = mb.spoofax.compiler.spoofaxcore.Shared.builder()
      .name("Module")
      .fileExtensions(listOf())
      .defaultBasePackageId("mb.module"),

    multilangAnalyzer = MultilangAnalyzerCompiler.LanguageProjectInput.builder()
      .rootModules(listOf("module-interface/modules")),

    builder = LanguageProjectCompiler.Input.builder()
      .languageSpecificationDependency(GradleDependency.module("$group:module-interface.spoofaxcore:$version"))
  ))
}
