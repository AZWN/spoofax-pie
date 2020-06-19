package mb.minisdf.spoofax.task;

import mb.common.token.Token;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageScope;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.ArrayList;

@LanguageScope
public class MSdfIdeTokenize implements TaskDef<ResourceKey, @Nullable ArrayList<? extends Token<?>>> {
    private final MSdfParse parse;

    @Inject public MSdfIdeTokenize(MSdfParse parse) {
        this.parse = parse;
    }

    @Override public String getId() {
        return MSdfIdeTokenize.class.getCanonicalName();
    }

    @Override
    public @Nullable ArrayList<? extends Token<?>> exec(ExecContext context, ResourceKey key) throws Exception {
        final @Nullable JSGLR1ParseResult parseResult = context.require(parse, new ResourceStringSupplier(key));
        return parseResult.getTokens().orElse(null);
    }
}
