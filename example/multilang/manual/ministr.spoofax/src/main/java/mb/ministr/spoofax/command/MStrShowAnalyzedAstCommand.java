package mb.ministr.spoofax.command;

import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.ministr.spoofax.task.MStrAnalyzeProject;
import mb.pie.api.Task;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandOutput;
import mb.spoofax.core.language.command.arg.ArgProvider;
import mb.spoofax.core.language.command.arg.Param;
import mb.spoofax.core.language.command.arg.ParamDef;
import mb.spoofax.core.language.command.arg.RawArgs;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;

import javax.inject.Inject;
import java.io.Serializable;

public class MStrShowAnalyzedAstCommand implements CommandDef<MStrShowAnalyzedAstCommand.Args>  {
    private final MStrAnalyzeProject analyzeProject;

    @Inject public MStrShowAnalyzedAstCommand(MStrAnalyzeProject analyzeProject) {
        this.analyzeProject = analyzeProject;
    }

    @Override public String getId() {
        return MStrShowAnalyzedAstCommand.class.getCanonicalName();
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
            Param.of("project", ResourcePath.class, true, ListView.of(ArgProvider.context(CommandContextType.Directory))),
            Param.of("level", Level.class, false)
        );
    }

    @Override
    public Args fromRawArgs(RawArgs rawArgs) {
        final ResourcePath projectDir = rawArgs.getOrThrow("project");
        final @Nullable Level level = rawArgs.getOrNull("level");
        return new Args(projectDir, level);
    }

    @Override
    public Task<CommandOutput> createTask(Args args) {
        return analyzeProject.createTask(new MStrAnalyzeProject.Input(args.projectDir, args.level));
    }

    public static class Args implements Serializable {
        private final ResourcePath projectDir;
        private final @Nullable Level level;

        public Args(ResourcePath projectDir, @Nullable Level level) {
            this.projectDir = projectDir;
            this.level = level;
        }
    }
}
