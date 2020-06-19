package mb.ministr.spoofax;

import mb.common.message.KeyedMessages;
import mb.common.region.Region;
import mb.common.style.Styling;
import mb.common.token.Token;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.completions.common.CompletionResult;
import mb.ministr.spoofax.command.LoglevelArgConverter;
import mb.ministr.spoofax.command.MStrShowAnalyzedAstCommand;
import mb.ministr.spoofax.task.MStrComplete;
import mb.ministr.spoofax.task.MStrIdeTokenize;
import mb.ministr.spoofax.task.MStrIndexAst;
import mb.ministr.spoofax.task.MStrParse;
import mb.ministr.spoofax.task.MStrPostStatix;
import mb.ministr.spoofax.task.MStrPreStatix;
import mb.ministr.spoofax.task.MStrStyle;
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
import mb.statix.multilang.AnalysisContext;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.MultiLangConfig;
import mb.statix.multilang.tasks.SmlBuildMessages;
import mb.statix.multilang.utils.ContextUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import java.util.ArrayList;

public class MiniStrInstance implements LanguageInstance {
    private final static SetView<String> extensions = SetView.of("mstr");

    private final MStrParse parse;
    private final MStrStyle style;
    private final MStrIdeTokenize tokenize;
    private final MStrComplete complete;

    private final SmlBuildMessages analyze;
    private final MStrPreStatix preStatix;
    private final MStrPostStatix postStatix;
    private final MStrIndexAst indexAst;

    private final MStrShowAnalyzedAstCommand showAnalyzedAstCommand;

    private final ITermFactory termFactory;
    private final AnalysisContextService analysisContextService;
    private final ResourceService resourceService;

    @Inject public MiniStrInstance(
        MStrParse parse,
        SmlBuildMessages analyze,
        MStrStyle style,
        MStrIdeTokenize tokenize,
        MStrComplete complete,
        MStrPreStatix preStatix,
        MStrPostStatix postStatix,
        MStrShowAnalyzedAstCommand showAnalyzedAstCommand,
        ITermFactory termFactory,
        AnalysisContextService analysisContextService,
        MStrIndexAst indexAst, ResourceService resourceService) {
        this.parse = parse;
        this.analyze = analyze;
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
        return "MiniStr";
    }

    @Override public SetView<String> getFileExtensions() {
        return extensions;
    }

    @Override public Task<@Nullable ArrayList<? extends Token<?>>> createTokenizeTask(ResourceKey resourceKey) {
        return tokenize.createTask(resourceKey);
    }

    @Override public Task<@Nullable Styling> createStyleTask(ResourceKey resourceKey) {
        return style.createTask(parse.createTokensSupplier(resourceKey));
    }

    @Override public Task<@Nullable CompletionResult> createCompletionTask(ResourceKey resourceKey, Region primarySelection) {
        return complete.createTask(new MStrComplete.Input(parse.createNullableRecoverableAstSupplier(resourceKey)));
    }

    @Override
    public Task<@Nullable KeyedMessages> createCheckTask(ResourcePath projectRoot) {
        MultiLangConfig config = ContextUtils.readYamlConfig(resourceService, projectRoot);
        String contextId = config.getLanguageContexts().getOrDefault("mb.ministr", "mini-sdf-str");
        AnalysisContext context = analysisContextService.getAnalysisContext(new ContextId(contextId));
        return analyze.createTask(new SmlBuildMessages.Input(projectRoot, context));
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
        return CliCommand.of("ministr", ListView.of(
            CliCommand.of("analyze", "Analyzes MiniStr sources and shows the analyzed AST", showAnalyzedAstCommand, ListView.of(
                CliParam.positional("project", 0, "DIR", "Project directory to analyze"),
                CliParam.positional("level", 1, "level", "Log level", new LoglevelArgConverter())
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
    public MStrParse parse() {
        return this.parse;
    }

    public MStrPreStatix preStatix() { return this.preStatix; }
    public MStrPostStatix postStatix() { return this.postStatix; }

    public ITermFactory termFactory() { return this.termFactory; }

    public MStrIndexAst indexAst() {
        return this.indexAst;
    }
}
