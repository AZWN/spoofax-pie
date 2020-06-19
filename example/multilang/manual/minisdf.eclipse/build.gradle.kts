plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
}

fun compositeBuild(name: String) = "$group:$name"

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))
  annotationProcessor(platform(compositeBuild("spoofax.depconstraints")))

  bundleApi(compositeBuild("spoofax.eclipse"))
  bundleApi(compositeBuild("spoofax.eclipse.externaldeps"))
  bundleApi(compositeBuild("statix.multilang.eclipse"))
  bundleApi(compositeBuild("statix.multilang.eclipse.externaldeps"))
  bundleApi(project(":minisdf.manual.eclipse.externaldeps"))

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")
}
