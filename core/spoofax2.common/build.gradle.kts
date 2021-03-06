plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))

  implementation(project(":common"))
  implementation(project(":stratego.common"))

  api("org.metaborg:resource")
  api("org.metaborg:log.api")

  implementation("org.metaborg:org.spoofax.interpreter.core")

  compileOnly("org.checkerframework:checker-qual-android")
}
