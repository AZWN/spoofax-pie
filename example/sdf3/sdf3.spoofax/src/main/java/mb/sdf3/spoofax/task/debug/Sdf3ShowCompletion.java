package mb.sdf3.spoofax.task.debug;

import mb.sdf3.spoofax.task.Sdf3Desugar;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.task.Sdf3ToCompletion;
import mb.spoofax.core.language.LanguageScope;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@LanguageScope
public class Sdf3ShowCompletion extends ShowTaskDef {
    @Inject public Sdf3ShowCompletion(
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3ToCompletion operation,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        super(parse, desugar, operation, strategoRuntimeProvider, "pp-SDF3-string", "completion insertions");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
