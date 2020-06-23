import mb.common.util.ListView
import mb.spoofax.compiler.cli.*
import mb.spoofax.compiler.command.*
import mb.spoofax.compiler.menu.*
import mb.spoofax.compiler.spoofaxcore.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.CommandContextType
import mb.spoofax.core.language.command.CommandExecutionType
import mb.spoofax.core.language.command.HierarchicalResourceType
import java.util.Optional

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.adapter")
}

spoofaxAdapterProject {
  languageProject.set(project(":minisdf"))
  settings.set(mb.spoofax.compiler.gradle.spoofaxcore.AdapterProjectSettings(
    parser = ParserCompiler.AdapterProjectInput.builder(),
    styler = StylerCompiler.AdapterProjectInput.builder(),
    completer = CompleterCompiler.AdapterProjectInput.builder(),
    strategoRuntime = StrategoRuntimeCompiler.AdapterProjectInput.builder(),
    multilangAnalyzer = MultilangAnalyzerCompiler.AdapterProjectInput.builder()
      .rootModule("mini-sdf/mini-sdf-typing")
      .preAnalysisStrategy("pre-analyze")
      .postAnalysisStrategy("post-analyze")
      .contextId("mini-sdf-str")
      .fileConstraint("mini-sdf/mini-sdf-typing!msdfProgramOK")
      .projectConstraint("mini-sdf/mini-sdf-typing!msdfProjectOK"),

    builder = run {
      AdapterProjectCompiler.Input.builder()
    }
  ))
}
