package mb.ministr.spoofax.task;

import dagger.Lazy;
import mb.common.message.Messages;
import mb.pie.api.Function;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.Supplier;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageScope;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadataManager;
import mb.statix.multilang.pie.SmlCheckTaskDef;
import mb.statix.multilang.pie.SmlSolveProject;
import mb.statix.multilang.pie.config.SmlBuildContextConfiguration;

import javax.inject.Inject;

@LanguageScope
public class MStrSmlCheck extends SmlCheckTaskDef {
    @Inject public MStrSmlCheck(
        MStrParse parse,
        SmlBuildContextConfiguration buildContextConfiguration,
        SmlSolveProject solveProject,
        Lazy<LanguageMetadataManager> languageMetadataManager
    ) {
        super(parseMessageSupplier(parse), buildContextConfiguration, solveProject, languageMetadataManager);
    }

    @Override
    public String getId() {
        return MStrSmlCheck.class.getCanonicalName();
    }

    private static Function<ResourceKey, Messages> parseMessageSupplier(MStrParse parse) {
        // Indirection needed for typing
        java.util.function.Function<ResourceKey, Supplier<String>> resourceReader = ResourceStringSupplier::new;
        return parse.createMessagesFunction().mapInput(resourceReader);
    }

    @Override protected LanguageId getLanguageId() {
        return new LanguageId("mb.ministr");
    }
}
