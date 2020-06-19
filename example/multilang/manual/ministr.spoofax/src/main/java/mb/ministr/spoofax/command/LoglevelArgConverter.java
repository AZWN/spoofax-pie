package mb.ministr.spoofax.command;

import mb.spoofax.core.language.command.arg.ArgConverter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;

public class LoglevelArgConverter implements ArgConverter<@Nullable Level> {
    @Override
    public @Nullable Level convert(String argStr) {
        try {
            return Level.parse(argStr);
        } catch(IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public Class<@Nullable Level> getOutputClass() {
        return Level.class;
    }
}
