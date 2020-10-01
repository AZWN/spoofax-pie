import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.gradle.spoofax2.plugin.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.spoofax2.language.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
}

dependencies {
  api(project(":module"))
}

languageProject {
  shared {
    name("MiniSdf")
    defaultPackageId("mb.minisdf")
  }
  compilerInput {
    withParser().run {
      startSymbol("MSDFStart")
    }
    withStyler()
    withMultilangAnalyzer().run {
      rootModules(listOf("mini-sdf/mini-sdf-typing"))
    }
    withStrategoRuntime()
  }
}

spoofax2BasedLanguageProject {
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime().run {
      copyCtree(true)
      copyClasses(false)
    }
    withMultilangAnalyzer()
    project
      .languageSpecificationDependency(GradleDependency.project(":minisdf.spoofaxcore"))
  }
}
