package mb.spoofax.core.language.taskdef;

import mb.common.result.Result;
import mb.pie.api.Supplier;

import javax.inject.Inject;

public class NullParser extends NullTaskDef<Supplier<String>, Result<?, ? extends Exception>> {
    @Inject public NullParser() {}
}
