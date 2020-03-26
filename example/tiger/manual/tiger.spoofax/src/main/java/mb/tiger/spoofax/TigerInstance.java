package mb.tiger.spoofax;

import mb.common.region.Region;
import mb.common.style.Styling;
import mb.common.token.Token;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.common.util.SetView;
import mb.completions.common.CompletionResult;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageInspection;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.cli.CliCommand;
import mb.spoofax.core.language.cli.CliParam;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.spoofax.core.language.menu.CommandAction;
import mb.spoofax.core.language.menu.MenuItem;
import mb.statix.spec.Spec;
import mb.tiger.spoofax.command.TigerCompileDirectoryCommand;
import mb.tiger.spoofax.command.TigerCompileFileAltCommand;
import mb.tiger.spoofax.command.TigerCompileFileCommand;
import mb.tiger.spoofax.command.TigerShowAnalyzedAstCommand;
import mb.tiger.spoofax.command.TigerShowDesugaredAstCommand;
import mb.tiger.spoofax.command.TigerShowParsedAstCommand;
import mb.tiger.spoofax.command.TigerShowPrettyPrintedTextCommand;
import mb.tiger.spoofax.task.TigerIdeCheck;
import mb.tiger.spoofax.task.TigerIdeTokenize;
import mb.tiger.spoofax.task.reusable.*;
import mb.tiger.spoofax.task.reusable.TigerPreAnalyzeTaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Set;

public class TigerInstance implements LanguageInstance {
    private final static SetView<String> extensions = SetView.of("tig");

    private final TigerParse parse;
    private final TigerIdeCheck tigerIdeCheck;
    private final TigerStyle style;
    private final TigerIdeTokenize tokenize;
    private final TigerCompleteTaskDef completeTaskDef;
    private final TigerStatixSpecTaskDef statixSpecTaskDef;
    private final TigerPrettyPrintTaskDef prettyPrintTaskDef;
    private final TigerPreAnalyzeTaskDef explicateTaskDef;
//    private final TigerDeexplicateTaskDef deexplicateTaskDef;

    private final TigerShowParsedAstCommand showParsedAstCommand;
    private final TigerShowPrettyPrintedTextCommand showPrettyPrintedTextCommand;
    private final TigerShowAnalyzedAstCommand showAnalyzedAstCommand;
    private final TigerShowDesugaredAstCommand showDesugaredAstCommand;
    private final TigerCompileFileCommand compileFileCommand;
    private final TigerCompileFileAltCommand altCompileFileCommand;
    private final TigerCompileDirectoryCommand compileDirectoryCommand;

    private final CollectionView<CommandDef<?>> commandDefs;
    private final CollectionView<AutoCommandRequest<?>> autoCommandDefs;


    @Inject public TigerInstance(
        TigerParse parse,
        TigerIdeCheck tigerIdeCheck,
        TigerStyle style,
        TigerIdeTokenize tokenize,
        TigerCompleteTaskDef completeTaskDef,
        TigerStatixSpecTaskDef statixSpecTaskDef,
        TigerPrettyPrintTaskDef prettyPrintTaskDef,
        TigerPreAnalyzeTaskDef explicateTaskDef,

        TigerShowParsedAstCommand showParsedAstCommand,
        TigerShowPrettyPrintedTextCommand showPrettyPrintedTextCommand,
        TigerShowAnalyzedAstCommand showAnalyzedAstCommand,
        TigerShowDesugaredAstCommand showDesugaredAstCommand,
        TigerCompileFileCommand compileFileCommand,
        TigerCompileFileAltCommand altCompileFileCommand,
        TigerCompileDirectoryCommand compileDirectoryCommand,

        Set<CommandDef<?>> commandDefs,
        Set<AutoCommandRequest<?>> autoCommandDefs
    ) {
        this.parse = parse;
        this.tigerIdeCheck = tigerIdeCheck;
        this.style = style;
        this.tokenize = tokenize;
        this.completeTaskDef = completeTaskDef;
        this.statixSpecTaskDef = statixSpecTaskDef;
        this.prettyPrintTaskDef = prettyPrintTaskDef;
        this.explicateTaskDef = explicateTaskDef;

        this.showParsedAstCommand = showParsedAstCommand;
        this.showPrettyPrintedTextCommand = showPrettyPrintedTextCommand;
        this.showAnalyzedAstCommand = showAnalyzedAstCommand;
        this.showDesugaredAstCommand = showDesugaredAstCommand;
        this.compileFileCommand = compileFileCommand;
        this.altCompileFileCommand = altCompileFileCommand;
        this.compileDirectoryCommand = compileDirectoryCommand;

        this.commandDefs = CollectionView.copyOf(commandDefs);
        this.autoCommandDefs = CollectionView.copyOf(autoCommandDefs);
    }


    @Override public String getDisplayName() {
        return "Tiger";
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

    @Override
    public Task<@Nullable CompletionResult> createCompleteTask(ResourceKey resourceKey, Region primarySelection) {
        return completeTaskDef.createTask(new TigerCompleteTaskDef.Input(
            resourceKey,
            primarySelection.getStartOffset(),
            parse.createAstSupplier(resourceKey),
            (c, t) -> prettyPrintTaskDef.createFunction().apply(c, new TigerPrettyPrintTaskDef.Input(c2 -> t)),
            (c, t) -> explicateTaskDef.createFunction().apply(c, new TigerPreAnalyzeTaskDef.Input(c2 -> t)),
            null
//            deexplicateFunction
        ));
    }

    public Task<@Nullable Spec> createStatixSpecTask() {
        return statixSpecTaskDef.createTask(new TigerStatixSpecTaskDef.Input(
            null    // TODO: Provide Statix spec parser task
        ));
    }

    @Override public LanguageInspection getInspection() {
        return LanguageInspection.singleFile(tigerIdeCheck::createTask);
    }


    @Override public CollectionView<CommandDef<?>> getCommandDefs() {
        return commandDefs;
    }

    @Override public CollectionView<AutoCommandRequest<?>> getAutoCommandRequests() {
        return autoCommandDefs;
    }


    @Override public CliCommand getCliCommand() {
        return CliCommand.of("tiger", "Tiger language command-line interface", ListView.of(
            CliCommand.of("parse", "Parses Tiger sources and shows the parsed AST", showParsedAstCommand, ListView.of(
                CliParam.positional("resource", 0, "FILE", "Source file to parse"),
                CliParam.option("region", ListView.of("-r", "--region"), false, "REGION", "Region in source file to parse")
            )),
            CliCommand.of("pretty-print", "Pretty-prints Tiger sources", showPrettyPrintedTextCommand, ListView.of(
                CliParam.positional("resource", 0, "FILE", "Source file to pretty-print"),
                CliParam.option("region", ListView.of("-r", "--region"), false, "REGION", "Region in source file to pretty-print")
            )),
            CliCommand.of("analyze", "Analyzes Tiger sources and shows the analyzed AST", showAnalyzedAstCommand, ListView.of(
                CliParam.positional("resource", 0, "FILE", "Source file to analyze"),
                CliParam.option("region", ListView.of("-r", "--region"), false, "REGION", "Region in source file to analyze")
            )),
            CliCommand.of("desugar", "Desugars Tiger sources and shows the desugared AST", showDesugaredAstCommand, ListView.of(
                CliParam.positional("resource", 0, "FILE", "Source file to desugar"),
                CliParam.option("region", ListView.of("-r", "--region"), false, "REGION", "Region in source file to desugar")
            )),
            CliCommand.of("compile-file", "Compiles Tiger sources and shows the compiled file", compileFileCommand, ListView.of(
                CliParam.positional("file", 0, "FILE", "File to compile")
            )),
            CliCommand.of("alt-compile-file", "Compiles Tiger sources in an alternative way and shows the compiled file", altCompileFileCommand, ListView.of(
                CliParam.positional("file", 0, "FILE", "File to compile"),
                CliParam.option("listDefNames", ListView.of("-l", "--no-defnames"), true, "", "Whether to list definition names intead of literal values"),
                CliParam.option("base64Encode", ListView.of("-b", "--base64"), false, "", "Whether to Base64 encode the result"),
                CliParam.option("compiledFileNameSuffix", ListView.of("-s", "--suffix"), false, "SUFFIX", "Suffix to append to the compiled file name")
            )),
            CliCommand.of("compile-dir", "Compiles Tiger sources in given directory and shows the compiled file", compileDirectoryCommand, ListView.of(
                CliParam.positional("dir", 0, "DIR", "Directory to compile")
            )))
        );
    }


    @Override public ListView<MenuItem> getMainMenuItems() {
        return getEditorContextMenuItems();
    }

    @Override public ListView<MenuItem> getResourceContextMenuItems() {
        return ListView.of(
            MenuItem.menu("Compile",
                CommandAction.builder().manualOnce(compileFileCommand).fileRequired().buildItem(),
                CommandAction.builder().manualOnce(compileDirectoryCommand).directoryRequired().buildItem(),
                CommandAction.builder().manualOnce(altCompileFileCommand, "- default").fileRequired().buildItem(),
                CommandAction.builder().manualOnce(altCompileFileCommand, "- list literal values instead", new RawArgs(MapView.of("listDefNames", false, "compiledFileNameSuffix", "litvals.aterm"))).fileRequired().buildItem(),
                CommandAction.builder().manualOnce(altCompileFileCommand, "- base64 encode", new RawArgs(MapView.of("base64Encode", true, "compiledFileNameSuffix", "defnames_base64.txt"))).fileRequired().buildItem(),
                CommandAction.builder().manualOnce(altCompileFileCommand, "- list literal values instead + base64 encode", new RawArgs(MapView.of("listDefNames", false, "base64Encode", true, "compiledFileNameSuffix", "litvals_base64.txt"))).fileRequired().buildItem(),
                CommandAction.builder().manualContinuous(altCompileFileCommand, "- default").buildItem(),
                CommandAction.builder().manualContinuous(altCompileFileCommand, "- list literal values instead", new RawArgs(MapView.of("listDefNames", false, "compiledFileNameSuffix", "litvals.aterm"))).fileRequired().buildItem(),
                CommandAction.builder().manualContinuous(altCompileFileCommand, "- base64 encode", new RawArgs(MapView.of("base64Encode", true, "compiledFileNameSuffix", "defnames_base64.txt"))).fileRequired().buildItem(),
                CommandAction.builder().manualContinuous(altCompileFileCommand, "- list literal values instead + base64 encode", new RawArgs(MapView.of("listDefNames", false, "base64Encode", true, "compiledFileNameSuffix", "litvals_base64.txt"))).fileRequired().buildItem()
            ),
            MenuItem.menu("Debug",
                MenuItem.menu("Syntax",
                    CommandAction.builder().manualOnce(showParsedAstCommand).buildItem(),
                    CommandAction.builder().manualOnce(showPrettyPrintedTextCommand).buildItem()
                ),
                MenuItem.menu("Static Semantics",
                    CommandAction.builder().manualOnce(showAnalyzedAstCommand).buildItem()
                ),
                MenuItem.menu("Transformations",
                    CommandAction.builder().manualOnce(showDesugaredAstCommand).buildItem()
                )
            )
        );
    }

    @Override public ListView<MenuItem> getEditorContextMenuItems() {
        return ListView.of(
            MenuItem.menu("Compile",
                CommandAction.builder().manualOnce(compileFileCommand).fileRequired().buildItem(),
                CommandAction.builder().manualOnce(altCompileFileCommand, "- default").fileRequired().buildItem(),
                CommandAction.builder().manualOnce(altCompileFileCommand, "- list literal values instead", new RawArgs(MapView.of("listDefNames", false, "compiledFileNameSuffix", "litvals.aterm"))).fileRequired().buildItem(),
                CommandAction.builder().manualOnce(altCompileFileCommand, "- base64 encode", new RawArgs(MapView.of("base64Encode", true, "compiledFileNameSuffix", "defnames_base64.txt"))).fileRequired().buildItem(),
                CommandAction.builder().manualOnce(altCompileFileCommand, "- list literal values instead + base64 encode", new RawArgs(MapView.of("listDefNames", false, "base64Encode", true, "compiledFileNameSuffix", "litvals_base64.txt"))).fileRequired().buildItem(),
                CommandAction.builder().manualContinuous(altCompileFileCommand, "- default").fileRequired().buildItem(),
                CommandAction.builder().manualContinuous(altCompileFileCommand, "- list literal values instead", new RawArgs(MapView.of("listDefNames", false, "compiledFileNameSuffix", "litvals.aterm"))).fileRequired().buildItem(),
                CommandAction.builder().manualContinuous(altCompileFileCommand, "- base64 encode", new RawArgs(MapView.of("base64Encode", true, "compiledFileNameSuffix", "defnames_base64.txt"))).fileRequired().buildItem(),
                CommandAction.builder().manualContinuous(altCompileFileCommand, "- list literal values instead + base64 encode", new RawArgs(MapView.of("listDefNames", false, "base64Encode", true, "compiledFileNameSuffix", "litvals_base64.txt"))).fileRequired().buildItem()
            ),
            MenuItem.menu("Debug",
                MenuItem.menu("Syntax",
                    CommandAction.builder().manualOnce(showParsedAstCommand).buildItem(),
                    CommandAction.builder().manualContinuous(showParsedAstCommand).buildItem(),
                    CommandAction.builder().manualOnce(showPrettyPrintedTextCommand).buildItem(),
                    CommandAction.builder().manualContinuous(showPrettyPrintedTextCommand).buildItem()
                ),
                MenuItem.menu("Static Semantics",
                    CommandAction.builder().manualOnce(showAnalyzedAstCommand).buildItem(),
                    CommandAction.builder().manualContinuous(showAnalyzedAstCommand).buildItem()
                ),
                MenuItem.menu("Transformations",
                    CommandAction.builder().manualOnce(showDesugaredAstCommand).buildItem(),
                    CommandAction.builder().manualContinuous(showDesugaredAstCommand).buildItem()
                )
            )
        );
    }
}
