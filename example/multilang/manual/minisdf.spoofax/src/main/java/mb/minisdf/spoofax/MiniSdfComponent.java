package mb.minisdf.spoofax;

import dagger.Component;
import mb.minisdf.MSdfParser;
import mb.pie.api.TaskDef;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.platform.PlatformComponent;
import mb.statix.multilang.MultiLangComponent;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Provider;
import java.util.Set;

@LanguageScope
@Component(
    modules = MiniSdfModule.class,
    dependencies = {PlatformComponent.class, MultiLangComponent.class}
)
public interface MiniSdfComponent extends LanguageComponent {
    @Override MiniSdfInstance getLanguageInstance();

    Provider<MSdfParser> getParser();

    StrategoRuntime getStrategoRuntime();

    Set<TaskDef<?, ?>> getTaskDefs();
}
