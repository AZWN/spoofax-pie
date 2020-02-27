plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

fun compositeBuild(name: String) = "$group:$name"

dependencies {
  // Platforms
  api(platform(compositeBuild("spoofax.depconstraints")))
  annotationProcessor(platform(compositeBuild("spoofax.depconstraints")))
  testAnnotationProcessor(platform(compositeBuild("spoofax.depconstraints")))

  // Main
  api(project(":tiger"))
  api(compositeBuild("spoofax.core"))
  api("org.metaborg:pie.api")
  api("org.metaborg:pie.dagger")
  api("com.google.dagger:dagger")

  // TODO: Include
  implementation(compositeBuild("statix.completions"))

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")

  // Test
  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.30")
  testImplementation("org.metaborg:pie.runtime")
  testImplementation("org.metaborg:pie.dagger")

  testCompileOnly("org.checkerframework:checker-qual-android")

  testAnnotationProcessor("com.google.dagger:dagger-compiler")
}

tasks.test {
  // Show standard out and err in tests.
  testLogging {
    events(org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT, org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR)
    showStandardStreams = true
  }
}
