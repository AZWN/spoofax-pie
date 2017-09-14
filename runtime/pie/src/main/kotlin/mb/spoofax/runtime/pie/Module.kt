package mb.spoofax.runtime.pie

import com.google.inject.Binder
import mb.spoofax.runtime.pie.builder.*
import mb.spoofax.runtime.pie.builder.core.*
import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.BuildCache
import mb.pie.runtime.core.impl.MapBuildCache
import mb.pie.runtime.builtin.util.LoggerBuildReporter

open class SpoofaxPieModule : PieModule() {
  override fun configure(binder: Binder) {
    super.configure(binder)

    binder.bindCache()
    binder.bindPie()
    binder.bindBuilders()
  }

  open protected fun Binder.bindCache() {
    bind<BuildCache>().to<MapBuildCache>()
  }

  override fun Binder.bindReporter() {
    bind<BuildReporter>().to<LoggerBuildReporter>()
  }

  open protected fun Binder.bindPie() {
    bind<PieSrv>().to<PieSrvImpl>().asSingleton()
  }

  open protected fun Binder.bindBuilders() {
    val builders = builderMapBinder()

    bindBuilder<GenerateLangSpecConfig>(builders, GenerateLangSpecConfig.id)
    bindBuilder<GenerateWorkspaceConfig>(builders, GenerateWorkspaceConfig.id)

    bindBuilder<GenerateTable>(builders, GenerateTable.id)
    bindBuilder<Parse>(builders, Parse.id)

    bindBuilder<GenerateStylerRules>(builders, GenerateStylerRules.id)
    bindBuilder<Style>(builders, Style.id)

    bindBuilder<CoreLoadLang>(builders, CoreLoadLang.id)
    bindBuilder<CoreLoadProj>(builders, CoreLoadProj.id)
    bindBuilder<CoreParse>(builders, CoreParse.id)
    bindBuilder<CoreAnalyze>(builders, CoreAnalyze.id)
    bindBuilder<CoreTrans>(builders, CoreTrans.id)
    bindBuilder<CoreBuild>(builders, CoreBuild.id)
    bindBuilder<CoreBuildLangSpec>(builders, CoreBuildLangSpec.id)
    bindBuilder<CoreBuildOrLoad>(builders, CoreBuildOrLoad.id)
    bindBuilder<CoreExtensions>(builders, CoreExtensions.id)
    bindBuilder<CoreStyle>(builders, CoreStyle.id)
  }
}