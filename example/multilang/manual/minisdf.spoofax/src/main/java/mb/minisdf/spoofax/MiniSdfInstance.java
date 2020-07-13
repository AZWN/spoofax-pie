package mb.minisdf.spoofax;

import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.common.style.Styling;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.completions.common.CompletionResult;
import mb.jsglr.common.JSGLRTokens;
import mb.minisdf.spoofax.command.MSdfShowAnalyzedAstCommand;
import mb.minisdf.spoofax.task.MSdfComplete;
import mb.minisdf.spoofax.task.MSdfIdeTokenize;
import mb.minisdf.spoofax.task.MSdfParse;
import mb.minisdf.spoofax.task.MSdfSmlCheck;
import mb.minisdf.spoofax.task.MSdfStyle;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.cli.CliCommand;
import mb.spoofax.core.language.cli.CliParam;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.menu.MenuItem;
import org.checkerframework.checker.nullness.qual.Nullable;
import javax.inject.Inject;

public class MiniSdfInstance implements LanguageInstance {
    private final static SetView<String> extensions = SetView.of("msdf");

    private final MSdfParse parse;
    private final MSdfStyle style;
    private final MSdfIdeTokenize tokenize;
    private final MSdfComplete complete;

    private final MSdfSmlCheck check;
    private final MSdfShowAnalyzedAstCommand showAnalyzedAstCommand;

    @Inject public MiniSdfInstance(
        MSdfParse parse,
        MSdfSmlCheck check,
        MSdfStyle style,
        MSdfIdeTokenize tokenize,
        MSdfComplete complete,
        MSdfShowAnalyzedAstCommand showAnalyzedAstCommand
    ) {
        this.parse = parse;
        this.check = check;
        this.style = style;
        this.tokenize = tokenize;
        this.complete = complete;
        this.showAnalyzedAstCommand = showAnalyzedAstCommand;
    }

    @Override public String getDisplayName() {
        return "MiniSdf";
    }

    @Override public SetView<String> getFileExtensions() {
        return extensions;
    }

    @Override public Task<Option<JSGLRTokens>> createTokenizeTask(ResourceKey resourceKey) {
        return tokenize.createTask(resourceKey);
    }

    @Override public Task<Option<Styling>> createStyleTask(ResourceKey resourceKey) {
        return style.createTask(parse.createRecoverableTokensSupplier(resourceKey).map(Result::ok));
    }

    @Override public Task<@Nullable CompletionResult> createCompletionTask(ResourceKey resourceKey, Region primarySelection) {
        return complete.createTask(new MSdfComplete.Input(parse.createRecoverableAstSupplier(resourceKey).map(Result::get)));
    }

    @Override
    public Task<@Nullable KeyedMessages> createCheckTask(ResourcePath projectRoot) {
        return check.createTask(projectRoot);
    }

    @Override public CollectionView<CommandDef<?>> getCommandDefs() {
        // TODO: Properly initialize command definitions
        return CollectionView.of();
    }

    @Override public CollectionView<AutoCommandRequest<?>> getAutoCommandRequests() {
        // TODO: Properly initialize command definitions
        return CollectionView.of();
    }

    @Override
    public CliCommand getCliCommand() {
        return CliCommand.of("minisdf", ListView.of(
            CliCommand.of("analyze", "Analyzes MiniSdf sources and shows the analyzed AST", showAnalyzedAstCommand, ListView.of(
                CliParam.positional("project", 0, "DIR", "Project directory to analyze")
            ))
        ));
    }

    @Override
    public ListView<MenuItem> getMainMenuItems() {
        // TODO: Properly initialize menu items
        return ListView.of();
    }

    @Override
    public ListView<MenuItem> getResourceContextMenuItems() {
        // TODO: Properly initialize menu items
        return ListView.of();
    }

    @Override
    public ListView<MenuItem> getEditorContextMenuItems() {
        // TODO: Properly initialize menu items
        return ListView.of();
    }
}
