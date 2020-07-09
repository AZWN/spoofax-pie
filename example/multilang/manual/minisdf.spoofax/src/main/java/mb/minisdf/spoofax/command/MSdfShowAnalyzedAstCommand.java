package mb.minisdf.spoofax.command;

import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.minisdf.spoofax.task.MSdfAnalyzeProject;
import mb.pie.api.Task;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.arg.ArgProvider;
import mb.spoofax.core.language.command.arg.Param;
import mb.spoofax.core.language.command.arg.ParamDef;
import mb.spoofax.core.language.command.arg.RawArgs;

import javax.inject.Inject;
import java.io.Serializable;

public class MSdfShowAnalyzedAstCommand implements CommandDef<MSdfShowAnalyzedAstCommand.Args>  {
    private final MSdfAnalyzeProject analyzeProject;

    @Inject public MSdfShowAnalyzedAstCommand(MSdfAnalyzeProject analyzeProject) {
        this.analyzeProject = analyzeProject;
    }

    @Override public String getId() {
        return MSdfShowAnalyzedAstCommand.class.getCanonicalName();
    }

    @Override public String getDisplayName() {
        return "Analyze project";
    }

    @Override public String getDescription() {
        return "Shows the analyzed AST of all the files in the project";
    }

    @Override public EnumSetView<CommandExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(CommandExecutionType.ManualOnce);
    }

    @Override
    public ParamDef getParamDef() {
        return new ParamDef(
            Param.of("project", ResourcePath.class, true, ListView.of(ArgProvider.context(CommandContextType.Directory)))
        );
    }

    @Override
    public Args fromRawArgs(RawArgs rawArgs) {
        final ResourcePath projectDir = rawArgs.getOrThrow("project");
        return new Args(projectDir);
    }

    @Override
    public Task<CommandFeedback> createTask(Args args) {
        return analyzeProject.createTask(args.projectDir);
    }

    public static class Args implements Serializable {
        private final ResourcePath projectDir;

        public Args(ResourcePath projectDir) {
            this.projectDir = projectDir;
        }
    }
}
