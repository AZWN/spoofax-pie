import mb.spoofax.compiler.gradle.spoofaxcore.*
import mb.spoofax.compiler.spoofaxcore.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.adapter")
}

dependencies {
  api("org.metaborg:stratego.build")
  api("org.metaborg:pie.task.java")

  // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
  compileOnly("com.google.code.findbugs:jsr305")

  testAnnotationProcessor(platform("$group:spoofax.depconstraints:$version"))
  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.30")
  testImplementation("org.metaborg:pie.runtime")
  testImplementation("org.metaborg:pie.dagger")
  testImplementation("com.google.jimfs:jimfs:1.1")
  testCompileOnly("org.checkerframework:checker-qual-android")
  testAnnotationProcessor("com.google.dagger:dagger-compiler")
}

spoofaxAdapterProject {
  languageProject.set(project(":stratego"))
  settings.set(AdapterProjectSettings(
    parser = ParserCompiler.AdapterProjectInput.builder(),
    styler = StylerCompiler.AdapterProjectInput.builder(),
    completer = CompleterCompiler.AdapterProjectInput.builder(),
    strategoRuntime = StrategoRuntimeCompiler.AdapterProjectInput.builder(),

    builder = run {
      val packageId = "mb.str.spoofax"
      val incrPackageId = "$packageId.incr"
      val taskPackageId = "$packageId.task"
      val commandPackageId = "$packageId.command"

      val builder = AdapterProjectCompiler.Input.builder()

      builder.addAdditionalModules(packageId, "JavaTasksModule")
      builder.addAdditionalModules(incrPackageId, "StrategoIncrModule")

      builder.classKind(ClassKind.Extended)
      builder.genComponent(packageId, "GeneratedStrategoComponent")
      builder.manualComponent(packageId, "StrategoComponent")

      builder.isMultiFile(true)
      builder.genCheckMultiTaskDef(taskPackageId, "GeneratedStrategoCheckMulti")
      builder.manualCheckMultiTaskDef(taskPackageId, "StrategoCheckMulti")

      builder.addTaskDefs(taskPackageId, "StrategoCompile")
      builder.addTaskDefs(taskPackageId, "StrategoAnalyze")

      val strBuildTaskPackageId = "mb.stratego.build.strincr"
      builder.addTaskDefs(strBuildTaskPackageId, "StrIncr")
      builder.addTaskDefs(strBuildTaskPackageId, "StrIncrAnalysis")
      builder.addTaskDefs(strBuildTaskPackageId, "Frontend")
      builder.addTaskDefs(strBuildTaskPackageId, "SubFrontend")
      builder.addTaskDefs(strBuildTaskPackageId, "LibFrontend")
      builder.addTaskDefs(strBuildTaskPackageId, "Backend")

      val pieTaskJavaPackageId = "mb.pie.task.java"
      builder.addTaskDefs(pieTaskJavaPackageId, "CompileJava")
      builder.addTaskDefs(pieTaskJavaPackageId, "CreateJar")

      builder
    }
  ))
}

// Additional dependencies which are injected into tests.
val classPathInjection = configurations.create("classPathInjection")
dependencies {
  classPathInjection(platform("$group:spoofax.depconstraints:$version"))
  classPathInjection("org.metaborg:org.strategoxt.strj")
}

tasks.test {
  // Pass classPathInjection to tests in the form of system properties
  dependsOn(classPathInjection)
  doFirst {
    // Wrap in doFirst to properly defer dependency resolution to the task execution phase.
    systemProperty("classPath", classPathInjection.resolvedConfiguration.resolvedArtifacts.map { it.file }.joinToString(File.pathSeparator))
  }
}
