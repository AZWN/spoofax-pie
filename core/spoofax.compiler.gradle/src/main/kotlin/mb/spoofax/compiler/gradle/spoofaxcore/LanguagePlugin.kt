@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.common.util.Properties
import mb.pie.runtime.PieBuilderImpl
import mb.spoofax.compiler.dagger.*
import mb.spoofax.compiler.spoofaxcore.*
import mb.spoofax.compiler.util.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.*
import java.io.IOException
import java.nio.charset.StandardCharsets

open class LanguageProjectSettings(
  val shared: Shared.Builder = Shared.builder(),

  val languageProject: LanguageProject.Builder = LanguageProject.builder(),
  val classloaderResources: ClassloaderResourcesCompiler.Input.Builder = ClassloaderResourcesCompiler.Input.builder(),
  val parser: ParserLanguageCompiler.Input.Builder? = null, // Optional
  val styler: StylerLanguageCompiler.Input.Builder? = null, // Optional
  val completer: CompleterLanguageCompiler.Input.Builder? = null, // Optional
  val strategoRuntime: StrategoRuntimeLanguageCompiler.Input.Builder? = null, // Optional
  val constraintAnalyzer: ConstraintAnalyzerLanguageCompiler.Input.Builder? = null, // Optional
  val multilangAnalyzer: MultilangAnalyzerLanguageCompiler.Input.Builder? = null, // Optional

  val builder: LanguageProjectCompiler.Input.Builder = LanguageProjectCompiler.Input.builder()
) {
  internal fun finalize(gradleProject: Project): LanguageProjectFinalized {
    // Attempt to load compiler properties from file.
    val spoofaxCompilerPropertiesFile = gradleProject.projectDir.resolve("spoofaxc.lock")
    val spoofaxCompilerProperties = Properties()
    if(spoofaxCompilerPropertiesFile.exists()) {
      spoofaxCompilerPropertiesFile.bufferedReader().use {
        try {
          spoofaxCompilerProperties.load(it)
        } catch(e: IOException) {
          gradleProject.logger.warn("Failed to load Spoofax compiler properties from file '$spoofaxCompilerPropertiesFile'", e)
        }
      }
    }

    // Build shared settings.
    val shared = shared
      .withPersistentProperties(spoofaxCompilerProperties)
      .build()

    // Build language project compiler settings.
    val project = gradleProject.toSpoofaxCompilerProject()
    val languageProject = this.languageProject.project(project).packageId(LanguageProject.Builder.defaultPackageId(shared)).build()
    val classloaderResources = this.classloaderResources.shared(shared).languageProject(languageProject).build()
    val parser = if(this.parser != null) this.parser.shared(shared).languageProject(languageProject).build() else null
    val styler = if(this.styler != null) this.styler.shared(shared).languageProject(languageProject).build() else null
    val completer = if(this.completer != null) this.completer.shared(shared).languageProject(languageProject).build() else null
    val strategoRuntime = if(this.strategoRuntime != null) this.strategoRuntime.shared(shared).languageProject(languageProject).build() else null
    val constraintAnalyzer = if(this.constraintAnalyzer != null) this.constraintAnalyzer.shared(shared).languageProject(languageProject).build() else null
    val multilangAnalyzer = if(this.multilangAnalyzer != null) this.multilangAnalyzer.shared(shared).languageProject(languageProject)
      .classloaderResources(classloaderResources.classloaderResources()).build() else null
    val builder = this.builder
      .shared(shared)
      .languageProject(languageProject)
      .classloaderResources(classloaderResources)
    if(parser != null) {
      builder.parser(parser)
    }
    if(styler != null) {
      builder.styler(styler)
    }
    if(completer != null) {
      builder.completer(completer)
    }
    if(strategoRuntime != null) {
      builder.strategoRuntime(strategoRuntime)
    }
    if(constraintAnalyzer != null) {
      builder.constraintAnalyzer(constraintAnalyzer)
    }
    if(multilangAnalyzer != null) {
      builder.multilangAnalyzer(multilangAnalyzer)
    }
    val input = builder.build()

    // Save compiler properties to file.
    shared.savePersistentProperties(spoofaxCompilerProperties)
    spoofaxCompilerPropertiesFile.parentFile.mkdirs()
    spoofaxCompilerPropertiesFile.createNewFile()
    spoofaxCompilerPropertiesFile.bufferedWriter().use {
      try {
        spoofaxCompilerProperties.storeWithoutDate(it)
        it.flush()
      } catch(e: IOException) {
        gradleProject.logger.warn("Failed to save Spoofax compiler properties to file '$spoofaxCompilerPropertiesFile'", e)
      }
    }

    val component = DaggerSpoofaxCompilerGradleComponent.builder()
      .spoofaxCompilerModule(SpoofaxCompilerModule(TemplateCompiler(Shared::class.java, StandardCharsets.UTF_8)))
      .spoofaxCompilerGradleModule(SpoofaxCompilerGradleModule({ PieBuilderImpl() }))
      .build()

    return LanguageProjectFinalized(shared, input, component)
  }

  internal fun addStatixDependencies(statixDependencies: List<Project>) {
    statixDependencies.forEach {
      val ext: LanguageProjectExtension = it.extensions.getByType()
      val factory = ext.settingsFinalized.input.multilangAnalyzer().get().specConfigFactory()
      this.multilangAnalyzer?.addDependencyFactories(factory)
    }
  }
}

open class LanguageProjectExtension(project: Project) {
  // statixDependencies must be in a separate property, since its finalized
  // value is used to check if the settings property can be finalized
  val statixDependencies: ListProperty<Project> = project.objects.listProperty()
  val settings: Property<LanguageProjectSettings> = project.objects.property()

  init {
    settings.convention(LanguageProjectSettings())
  }

  companion object {
    internal const val id = "spoofaxLanguageProject"
    private const val name = "Spoofax language project"
  }

  internal val settingsFinalized: LanguageProjectFinalized by lazy {
    project.logger.debug("Finalizing $name settings in $project")
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("$name settings in $project have not been set")
    }
    val settings = settings.get()
    val statixDependencies = statixDependenciesFinalized

    if(settings.multilangAnalyzer == null) {
      if(statixDependencies.isNotEmpty()) {
        project.logger.warn("Statix dependencies given, but no multilang analyzer configuration set. Ignoring statix dependencies")
      }
    } else {
      settings.addStatixDependencies(statixDependencies)
    }
    settings.finalize(project)
  }

  internal val statixDependenciesFinalized: List<Project> by lazy {
    project.logger.debug("Finalizing $name statix dependencies in $project")
    statixDependencies.finalizeValue()
    if(!statixDependencies.isPresent) {
      throw GradleException("$name statix dependencies in $project have not been set")
    }
    statixDependencies.get()
  }
}

internal class LanguageProjectFinalized(
  val shared: Shared,
  val input: LanguageProjectCompiler.Input,
  val component: SpoofaxCompilerGradleComponent
) {
  val resourceService = component.resourceService
  val pie = component.pie
  val compiler = component.languageProjectCompiler
}

internal fun Project.whenLanguageProjectFinalized(closure: () -> Unit) = whenFinalized<LanguageProjectExtension> {
  val extension: LanguageProjectExtension = extensions.getByType()
  // Project is fully finalized only iff all dependencies are finalized as well
  extension.statixDependenciesFinalized.whenAllLanguageProjectsFinalized(closure)
}

@Suppress("unused")
open class LanguagePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = LanguageProjectExtension(project)
    project.extensions.add(LanguageProjectExtension.id, extension)

    project.plugins.apply(JavaLibraryPlugin::class.java)
    project.plugins.apply("org.metaborg.spoofax.gradle.base")

    project.afterEvaluate {
      extension.statixDependenciesFinalized.whenAllLanguageProjectsFinalized {
        configure(project, extension.settingsFinalized)
      }
    }
  }

  private fun configure(project: Project, finalized: LanguageProjectFinalized) {
    configureProject(project, finalized)
    configureCompileTask(project, finalized)
    configureCopySpoofaxLanguageTasks(project, finalized)
  }

  private fun configureProject(project: Project, finalized: LanguageProjectFinalized) {
    project.configureGeneratedSources(project.toSpoofaxCompilerProject(), finalized.resourceService)
    finalized.compiler.getDependencies(finalized.input).forEach {
      it.addToDependencies(project)
    }
  }

  private fun configureCompileTask(project: Project, finalized: LanguageProjectFinalized) {
    val input = finalized.input
    val compileTask = project.tasks.register("spoofaxCompileLanguageProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { finalized.resourceService.toLocalFile(it) })

      doLast {
        project.deleteGenSourceSpoofaxDirectory(input.languageProject().project(), finalized.resourceService)
        finalized.pie.newSession().use { session ->
          session.require(finalized.compiler.createTask(input))
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureCopySpoofaxLanguageTasks(project: Project, finalized: LanguageProjectFinalized) {
    val input = finalized.input
    val destinationPackage = input.languageProject().packagePath()
    val includeStrategoClasses = input.strategoRuntime().map { it.copyClasses() }.orElse(false)
    val copyResources = finalized.compiler.getCopyResources(input)

    // Add language dependency.
    val languageDependency = input.languageSpecificationDependency().caseOf()
      .project<Dependency> { project.dependencies.project(it) }
      .module { project.dependencies.module(it.toGradleNotation()) }
      .files { project.dependencies.create(project.files(it)) }
    project.dependencies.add("compileLanguage", languageDependency)

    // Unpack the '.spoofax-language' archive.
    val languageFiles = project.configurations.getByName("languageFiles")
    val unpackSpoofaxLanguageDir = "${project.buildDir}/unpackedSpoofaxLanguage/"
    val unpackSpoofaxLanguageTask = project.tasks.register<Sync>("unpackSpoofaxLanguage") {
      inputs.property("input", input)
      dependsOn(languageFiles)
      from({ languageFiles.map { project.zipTree(it) } })  /* Closure inside `from` to defer evaluation until task execution time */
      into(unpackSpoofaxLanguageDir)

      val allCopyResources = copyResources.toMutableList()
      if(includeStrategoClasses) {
        allCopyResources.add("target/metaborg/stratego.jar")
      }
      include(allCopyResources)
    }

    // Copy resources into `mainSourceSet.java.outputDir` and `testSourceSet.java.outputDir`, so they end up in the target package.
    val resourcesCopySpec = project.copySpec {
      from(unpackSpoofaxLanguageDir)
      include(copyResources)
    }
    val strategoCopySpec = project.copySpec {
      from(project.zipTree("$unpackSpoofaxLanguageDir/target/metaborg/stratego.jar"))
      exclude("META-INF")
    }
    val copyMainTask = project.tasks.register<Copy>("copyMainResources") {
      dependsOn(unpackSpoofaxLanguageTask)
      into(project.the<SourceSetContainer>()["main"].java.outputDir)
      into(destinationPackage) { with(resourcesCopySpec) }
      if(includeStrategoClasses) {
        into(".") { with(strategoCopySpec) }
      }
    }
    project.tasks.getByName(JavaPlugin.CLASSES_TASK_NAME).dependsOn(copyMainTask)
    val copyTestTask = project.tasks.register<Copy>("copyTestResources") {
      dependsOn(unpackSpoofaxLanguageTask)
      into(project.the<SourceSetContainer>()["test"].java.outputDir)
      into(destinationPackage) { with(resourcesCopySpec) }
      if(includeStrategoClasses) {
        into(".") { with(strategoCopySpec) }
      }
    }
    project.tasks.getByName(JavaPlugin.TEST_CLASSES_TASK_NAME).dependsOn(copyTestTask)
  }
}

internal fun List<Project>.whenAllLanguageProjectsFinalized(closure: () -> Unit) {
  if(isEmpty()) {
    // No dependencies to wait for, so execute immediately
    closure()
  } else {
    // After first project in list is finalized, invoke wait for the others
    first().whenLanguageProjectFinalized {
      drop(1).whenAllLanguageProjectsFinalized(closure)
    }
  }
}
