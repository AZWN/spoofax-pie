package mb.minisdf.spoofax.task;

import dagger.Lazy;
import mb.common.message.Messages;
import mb.pie.api.Function;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.Supplier;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageScope;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.pie.SmlBuildMessages;
import mb.statix.multilang.pie.SmlCheckTaskDef;
import mb.statix.multilang.pie.config.SmlBuildContextConfiguration;

import javax.inject.Inject;

@LanguageScope
public class MSdfSmlCheck extends SmlCheckTaskDef {

    @Inject public MSdfSmlCheck(
        MSdfParse parse,
        SmlBuildContextConfiguration buildContextConfiguration,
        SmlBuildMessages buildMessages,
        Lazy<AnalysisContextService> analysisContextService
    ) {
        super(parseMessageSupplier(parse), buildContextConfiguration, buildMessages, analysisContextService);
    }

    @Override
    public String getId() {
        return MSdfSmlCheck.class.getCanonicalName();
    }

    private static Function<ResourceKey, Messages> parseMessageSupplier(MSdfParse parse) {
        // Indirection needed for typing
        java.util.function.Function<ResourceKey, Supplier<String>> resourceReader = ResourceStringSupplier::new;
        return parse.createMessagesFunction().mapInput(resourceReader);
    }

    @Override protected LanguageId getLanguageId() {
        return new LanguageId("mb.minisdf");
    }
}
