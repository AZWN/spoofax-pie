plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.gradle.base")
  id("de.set.ecj") // Use ECJ to speed up compilation of Stratego's generated Java files.
}

sourceSets {
  main {
    java {
      srcDir("$buildDir/generated/sources/spoofax/java")
    }
  }
}

fun compositeBuild(name: String) = "$group:$name"

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))

  api(compositeBuild("spoofax.compiler.interfaces"))
  api(compositeBuild("common"))

  compileOnly("org.checkerframework:checker-qual-android")

  testImplementation("org.metaborg:log.backend.noop")

  testCompileOnly("org.checkerframework:checker-qual-android")
}

fun copySpoofaxLanguageResources(
  dependency: Dependency,
  destinationPackage: String,
  includeStrategoClasses: Boolean,
  includeStrategoJavastratClasses: Boolean,
  vararg resources: String
) {
  val allResources = resources.toMutableList()
  if(includeStrategoClasses) {
    allResources.add("target/metaborg/stratego.jar")
  }
  if(includeStrategoJavastratClasses) {
    allResources.add("target/metaborg/stratego-javastrat.jar")
  }

  // Add language dependency.
  dependencies.add("compileLanguage", dependency)

  // Unpack the '.spoofax-language' archive.
  val languageFiles = project.configurations.getByName("languageFiles")
  val unpackSpoofaxLanguageDir = "$buildDir/unpackedSpoofaxLanguage/"
  val unpackSpoofaxLanguageTask = tasks.register<Sync>("unpackSpoofaxLanguage") {
    dependsOn(languageFiles)
    from({ languageFiles.map { project.zipTree(it) } })  /* Closure inside `from` to defer evaluation until task execution time */
    into(unpackSpoofaxLanguageDir)
    include(allResources)
    include { it.path.endsWith(".spec.aterm") }
  }
  // Copy resources into `mainSourceSet.java.outputDir` and `testSourceSet.java.outputDir`, so they end up in the target package.
  val resourcesCopySpec = copySpec {
    from(unpackSpoofaxLanguageDir)
    include(*resources)
  }
  val statixCopySpec = copySpec {
    from(unpackSpoofaxLanguageDir)
    include(listOf("src-gen/statix/**/*.spec.aterm"))
  }
  val strategoCopySpec = copySpec {
    from(project.zipTree("$unpackSpoofaxLanguageDir/target/metaborg/stratego.jar"))
    exclude("META-INF")
  }
  val strategoJavastratCopySpec = copySpec {
    from(project.zipTree("$unpackSpoofaxLanguageDir/target/metaborg/stratego-javastrat.jar"))
    exclude("META-INF")
  }
  val copyMainTask = tasks.register<Copy>("copyMainResources") {
    dependsOn(unpackSpoofaxLanguageTask)
    into(sourceSets.main.get().java.outputDir)
    into(destinationPackage) { with(resourcesCopySpec) }
    into(destinationPackage) { with(statixCopySpec) }
    if(includeStrategoClasses) {
      into(".") { with(strategoCopySpec) }
    }
    if(includeStrategoJavastratClasses) {
      into(".") { with(strategoJavastratCopySpec) }
    }
  }
  tasks.getByName(JavaPlugin.CLASSES_TASK_NAME).dependsOn(copyMainTask)
  val copyTestTask = tasks.register<Copy>("copyTestResources") {
    dependsOn(unpackSpoofaxLanguageTask)
    into(sourceSets.test.get().java.outputDir)
    into(destinationPackage) { with(resourcesCopySpec) }
    if(includeStrategoClasses) {
      into(".") { with(strategoCopySpec) }
    }
    if(includeStrategoJavastratClasses) {
      into(".") { with(strategoJavastratCopySpec) }
    }
  }
  tasks.getByName(JavaPlugin.TEST_CLASSES_TASK_NAME).dependsOn(copyTestTask)
}
copySpoofaxLanguageResources(
  dependencies.create(compositeBuild("signature-interface.spoofaxcore")),
  "mb/signature",
  false,
  false
)

ecj {
  toolVersion = "3.20.0"
}
