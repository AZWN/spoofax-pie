package mb.minisdf.spoofax.task;

import mb.common.option.Option;
import mb.jsglr.common.JSGLRTokens;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageScope;

import javax.inject.Inject;
import java.io.IOException;

@LanguageScope
public class MSdfIdeTokenize implements TaskDef<ResourceKey, Option<JSGLRTokens>> {
    private final MSdfParse parse;

    @Inject public MSdfIdeTokenize(MSdfParse parse) {
        this.parse = parse;
    }

    @Override public String getId() {
        return MSdfIdeTokenize.class.getCanonicalName();
    }

    @Override
    public Option<JSGLRTokens> exec(ExecContext context, ResourceKey key) throws IOException {
        return context.require(parse.createTokensSupplier(key)).ok();
    }
}
