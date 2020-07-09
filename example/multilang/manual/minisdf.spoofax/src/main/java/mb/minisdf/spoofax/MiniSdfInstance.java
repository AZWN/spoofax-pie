package mb.minisdf.spoofax;

import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.common.style.Styling;
import mb.common.token.Token;
import mb.common.token.Tokens;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.completions.common.CompletionResult;
import mb.jsglr.common.JSGLRTokens;
import mb.minisdf.spoofax.command.MSdfShowAnalyzedAstCommand;
import mb.minisdf.spoofax.task.MSdfComplete;
import mb.minisdf.spoofax.task.MSdfIdeTokenize;
import mb.minisdf.spoofax.task.MSdfIndexAst;
import mb.minisdf.spoofax.task.MSdfParse;
import mb.minisdf.spoofax.task.MSdfPostStatix;
import mb.minisdf.spoofax.task.MSdfPreStatix;
import mb.minisdf.spoofax.task.MSdfSmlCheck;
import mb.minisdf.spoofax.task.MSdfStyle;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.cli.CliCommand;
import mb.spoofax.core.language.cli.CliParam;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.menu.MenuItem;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.MultiLangConfig;
import mb.statix.multilang.pie.SmlBuildMessages;
import mb.statix.multilang.utils.ContextUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import java.util.ArrayList;

public class MiniSdfInstance implements LanguageInstance {
    private final static SetView<String> extensions = SetView.of("msdf");

    private final MSdfParse parse;
    private final MSdfStyle style;
    private final MSdfIdeTokenize tokenize;
    private final MSdfComplete complete;

    private final MSdfSmlCheck check;
    private final MSdfPreStatix preStatix;
    private final MSdfIndexAst indexAst;
    private final MSdfPostStatix postStatix;

    private final MSdfShowAnalyzedAstCommand showAnalyzedAstCommand;

    private final ITermFactory termFactory;
    private final AnalysisContextService analysisContextService;
    private final ResourceService resourceService;

    @Inject public MiniSdfInstance(
        MSdfParse parse,
        MSdfSmlCheck check,
        MSdfStyle style,
        MSdfIdeTokenize tokenize,
        MSdfComplete complete,
        MSdfPreStatix preStatix,
        MSdfIndexAst indexAst, MSdfPostStatix postStatix,
        MSdfShowAnalyzedAstCommand showAnalyzedAstCommand,
        ITermFactory termFactory, AnalysisContextService analysisContextService, ResourceService resourceService) {
        this.parse = parse;
        this.check = check;
        this.style = style;
        this.tokenize = tokenize;
        this.complete = complete;
        this.preStatix = preStatix;
        this.indexAst = indexAst;
        this.postStatix = postStatix;
        this.showAnalyzedAstCommand = showAnalyzedAstCommand;
        this.termFactory = termFactory;
        this.analysisContextService = analysisContextService;
        this.resourceService = resourceService;
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

    // Additionally provided methods
    public MSdfParse parse() {
        return this.parse;
    }

    public MSdfPreStatix preStatix() { return this.preStatix; }
    public MSdfPostStatix postStatix() { return this.postStatix; }

    public ITermFactory termFactory() { return this.termFactory; }

    public MSdfIndexAst indexAst() { return this.indexAst; }
}
