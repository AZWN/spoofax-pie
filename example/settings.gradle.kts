rootProject.name = "spoofax3.example.root"

pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

if(org.gradle.util.VersionNumber.parse(gradle.gradleVersion).major < 6) {
  enableFeaturePreview("GRADLE_METADATA")
}

// Only include composite builds when this is the root project (it has no parent), for example when running Gradle tasks
// from the command-line. Otherwise, the parent project (spoofax.root) will include these composite builds.
if(gradle.parent == null) {
  includeBuild("../core")
}

fun String.includeProject(id: String, path: String = "$this/$id") {
  include(id)
  project(":$id").projectDir = file(path)
}

"tiger/spoofaxcore".run {
  includeProject("tiger.spoofaxcore")
}
"tiger/manual".run {
  includeProject("tiger", "tiger/manual/tiger")
  includeProject("tiger.spoofax", "tiger/manual/tiger.spoofax")
  includeProject("tiger.cli", "tiger/manual/tiger.cli")
  includeProject("tiger.eclipse.externaldeps", "tiger/manual/tiger.eclipse.externaldeps")
  includeProject("tiger.eclipse", "tiger/manual/tiger.eclipse")
  includeProject("tiger.intellij", "tiger/manual/tiger.intellij")
}

"mod".run {
  includeProject("mod.spoofaxcore")
  includeProject("mod")
  includeProject("mod.spoofax")
  includeProject("mod.cli")
  includeProject("mod.eclipse.externaldeps")
  includeProject("mod.eclipse")
  includeProject("mod.intellij")
}

/* "sdf3".run {
  includeProject("sdf3")
  includeProject("sdf3.spoofax")
  includeProject("sdf3.cli")
  includeProject("sdf3.eclipse.externaldeps")
  includeProject("sdf3.eclipse")
  includeProject("sdf3.intellij")
} */

"stratego".run {
  includeProject("stratego")
  includeProject("stratego.spoofax")
  includeProject("stratego.cli")
  includeProject("stratego.eclipse.externaldeps")
  includeProject("stratego.eclipse")
  includeProject("stratego.intellij")
}

"multilang/spoofaxcore".run {
  includeProject("minisdf.spoofaxcore")
  includeProject("ministr.spoofaxcore")
}

"multilang/manual".run {
  includeProject("minisdf.manual", "multilang/manual/minisdf")
  includeProject("minisdf.manual.spoofax", "multilang/manual/minisdf.spoofax")
  includeProject("minisdf.manual.eclipse.externaldeps", "multilang/manual/minisdf.eclipse.externaldeps")
  includeProject("minisdf.manual.eclipse", "multilang/manual/minisdf.eclipse")

  includeProject("ministr.manual", "multilang/manual/ministr")
  includeProject("ministr.manual.spoofax", "multilang/manual/ministr.spoofax")
  includeProject("ministr.manual.eclipse.externaldeps", "multilang/manual/ministr.eclipse.externaldeps")
  includeProject("ministr.manual.eclipse", "multilang/manual/ministr.eclipse")

  includeProject("multilang.manual.cli", "multilang/manual/multilang.cli")
  includeProject("multilang.manual.test", "multilang/manual/multilang.test")
  includeProject("multilang.manual.eclipse", "multilang/manual/multilang.eclipse")
}

"multilang/generated".run {
  includeProject("minisdf")
  includeProject("minisdf.spoofax")
  includeProject("minisdf.eclipse.externaldeps")
  includeProject("minisdf.eclipse")
  includeProject("minisdf.cli")

  includeProject("ministr")
  includeProject("ministr.spoofax")
  includeProject("ministr.eclipse.externaldeps")
  includeProject("ministr.eclipse")
  includeProject("ministr.cli")

  includeProject("multilang.eclipse")
}
