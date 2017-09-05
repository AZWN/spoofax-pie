package mb.spoofax.runtime.pie.builder.core

import mb.pie.runtime.core.BuildContext
import mb.pie.runtime.core.Builder
import mb.pie.runtime.core.OutTransient
import mb.pie.runtime.core.PathStampers
import mb.vfs.path.PPath
import org.metaborg.core.build.CommonPaths
import org.metaborg.core.language.IComponentCreationConfigRequest
import org.metaborg.core.language.ILanguageImpl

class CoreLoadLang : Builder<PPath, OutTransient<ILanguageImpl>> {
  companion object {
    val id = "coreLoadLang"
  }

  override val id = Companion.id
  override fun BuildContext.build(input: PPath): OutTransient<ILanguageImpl> {
    val spoofax = Spx.spoofax()
    val resource = input.fileObject
    val request: IComponentCreationConfigRequest
    if (resource.isFile) {
      request = spoofax.languageComponentFactory.requestFromArchive(resource)
      require(input, PathStampers.hash)
    } else {
      request = spoofax.languageComponentFactory.requestFromDirectory(resource)
      val paths = CommonPaths(resource)
      require(paths.targetMetaborgDir().pPath, PathStampers.hash)
    }
    val config = spoofax.languageComponentFactory.createConfig(request)
    val component = spoofax.languageService.add(config)
    val impl = component.contributesTo().first()
    return OutTransient(impl)
  }
}

fun BuildContext.loadLangRaw(input: PPath) = requireOutput(CoreLoadLang::class.java, input)
fun BuildContext.loadLang(input: PPath) = loadLangRaw(input).v