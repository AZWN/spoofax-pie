plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.eclipse")
}

spoofaxEclipseProject {
  eclipseExternaldepsProject.set(project(":ministr.eclipse.externaldeps"))
  adapterProject.set(project(":ministr.spoofax"))
}
