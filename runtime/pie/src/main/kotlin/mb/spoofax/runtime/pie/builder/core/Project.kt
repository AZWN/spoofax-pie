package mb.spoofax.runtime.pie.builder.core

import mb.pie.runtime.core.BuildContext
import mb.pie.runtime.core.Builder
import mb.pie.runtime.core.OutTransient
import mb.vfs.path.PPath
import org.metaborg.core.project.IProject
import org.metaborg.core.project.ISimpleProjectService

class CoreLoadProj : Builder<PPath, OutTransient<CoreLoadProj.Project>> {
  companion object {
    val id = "coreLoadProj"
  }

  class Project(val proj: IProject) {
    val loc get() = proj.location()
    val dir get() = loc.pPath
  }

  override val id = Companion.id
  override fun BuildContext.build(input: PPath): OutTransient<Project> {
    val spoofax = Spx.spoofax()
    val projLoc = input.fileObject
    var project = spoofax.projectService.get(projLoc)
    if (project == null) {
      project = spoofax.injector.getInstance(ISimpleProjectService::class.java).create(projLoc)
    }
    return OutTransient(Project(project!!))
  }
}

fun BuildContext.loadProj(input: PPath) = requireOutput(CoreLoadProj::class.java, input).v
