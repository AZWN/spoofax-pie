plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  implementation(platform("$group:spoofax.depconstraints:$version"))
  annotationProcessor(platform("$group:spoofax.depconstraints:$version"))

  api("org.metaborg:statix.solver")
  api("org.metaborg:statix.multilang")

  implementation(project(":minisdf.manual.spoofax"))
  implementation(project(":ministr.manual.spoofax"))

  testAnnotationProcessor(platform("$group:spoofax.depconstraints:$version"))
}

tasks.test {
  // HACK: skip if not in devenv composite build, as that is not using the latest version of SDF3.
  if(gradle.parent == null || gradle.parent!!.rootProject.name != "devenv") {
    onlyIf { false }
  }

  // Show standard out and err in tests during development.
  testLogging {
    events(org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT, org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR)
    showStandardStreams = true
  }
}
