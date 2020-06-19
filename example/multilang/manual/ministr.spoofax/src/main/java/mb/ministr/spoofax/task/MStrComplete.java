package mb.ministr.spoofax.task;

import mb.common.style.StyleName;
import mb.common.util.ListView;
import mb.completions.common.CompletionProposal;
import mb.completions.common.CompletionResult;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.spoofax.core.language.LanguageScope;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;


@LanguageScope
public class MStrComplete implements TaskDef<MStrComplete.Input, @Nullable CompletionResult> {

    public static class Input implements Serializable {
        public final Supplier<@Nullable IStrategoTerm> astProvider;

        public Input(Supplier<IStrategoTerm> astProvider) {
            this.astProvider = astProvider;
        }
    }

    @Inject public MStrComplete() {}

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Override
    public @Nullable CompletionResult exec(ExecContext context, Input input) throws Exception {

        // 1) Get the file in which code completion is invoked & parse the file with syntactic completions enabled, resulting in an AST with placeholders
        //    ==> This should be done by specifying the correct astProvider
        // TODO: get the ast in 'completion mode', with placeholders
        @Nullable IStrategoTerm ast = input.astProvider.get(context);
        if (ast == null) return null;   // Cannot complete when we don't get an AST.

        // 3) Find the placeholder closest to the caret <- that's the one we want to complete
        //    TODO: What do we do when there are no placeholders? E.g., invoking code completion on a complete file?
        // 4) Get the solver state of the program (whole project), which should have some remaining constraints
        //    on the placeholder.
        //    TODO: What to do when the file is semantically incorrect? Recovery?
        // 5) Invoke the completer on the solver state, indicating the placeholder for which we want completions
        // 6) Get the possible completions back, as a list of ASTs with new solver states
        // 7) Format each completion as a proposal, with pretty-printed text
        // 8) Insert the selected completion: insert the pretty-printed text in the code,
        //    and (maybe?) add the solver state to the current solver state

        return new CompletionResult(ListView.of(
            new CompletionProposal("mypackage", "description", "", "", "mypackage", Objects.requireNonNull(StyleName.fromString("meta.package")), ListView.of(), false),
            new CompletionProposal("myclass", "description", "", "T", "mypackage", Objects.requireNonNull(StyleName.fromString("meta.class")), ListView.of(), false)
        ), true);
    }
}
