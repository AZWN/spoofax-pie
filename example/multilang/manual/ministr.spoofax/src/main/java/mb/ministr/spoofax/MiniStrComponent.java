package mb.ministr.spoofax;

import dagger.Component;
import dagger.Lazy;
import mb.pie.api.Pie;
import mb.pie.api.TaskDef;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.platform.PlatformComponent;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.MultiLangComponent;
import mb.statix.multilang.SupportsMLA;
import mb.stratego.common.StrategoRuntime;

import java.util.Set;

@LanguageScope
@Component(
    modules = MiniStrModule.class,
    dependencies = {PlatformComponent.class, MultiLangComponent.class}
)
public interface MiniStrComponent extends LanguageComponent, SupportsMLA {
    @Override MiniStrInstance getLanguageInstance();
}
