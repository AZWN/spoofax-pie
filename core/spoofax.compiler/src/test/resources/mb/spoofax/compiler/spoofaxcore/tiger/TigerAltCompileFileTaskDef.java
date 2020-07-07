package mb.tiger.spoofax.task;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.language.command.CommandFeedbacks;
import mb.spoofax.core.language.command.CommandFeedback;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

@LanguageScope
public class TigerAltCompileFileTaskDef implements TaskDef<TigerAltCompileFileTaskDef.Args, CommandFeedback> {
    public static class Args implements Serializable {
        final ResourcePath file;
        final boolean listDefNames;
        final boolean base64Encode;
        final String compiledFileNameSuffix;

        public Args(ResourcePath file, boolean listDefNames, boolean base64Encode, String compiledFileNameSuffix) {
            this.file = file;
            this.listDefNames = listDefNames;
            this.base64Encode = base64Encode;
            this.compiledFileNameSuffix = compiledFileNameSuffix;
        }

        @Override public boolean equals(@Nullable Object obj) {
            if(this == obj) return true;
            if(obj == null || getClass() != obj.getClass()) return false;
            final Args other = (Args)obj;
            return listDefNames == other.listDefNames &&
                base64Encode == other.base64Encode &&
                file.equals(other.file) &&
                compiledFileNameSuffix.equals(other.compiledFileNameSuffix);
        }

        @Override public int hashCode() {
            return Objects.hash(file, listDefNames, base64Encode, compiledFileNameSuffix);
        }

        @Override public String toString() {
            return file.toString();
        }
    }


    private final TigerParse parse;
    private final TigerListDefNames listDefNames;
    private final TigerListLiteralVals listLiteralVals;
    private final ResourceService resourceService;


    @Inject
    public TigerAltCompileFileTaskDef(TigerParse parse, TigerListDefNames listDefNames, TigerListLiteralVals listLiteralVals, ResourceService resourceService) {
        this.parse = parse;
        this.listDefNames = listDefNames;
        this.listLiteralVals = listLiteralVals;
        this.resourceService = resourceService;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args input) throws Exception {
        final ResourcePath file = input.file;

        final Supplier<@Nullable IStrategoTerm> astSupplier = parse.createAstProvider(file);
        @Nullable String str;
        if(input.listDefNames) {
            str = context.require(listDefNames, astSupplier);
        } else {
            str = context.require(listLiteralVals, astSupplier);
        }

        //noinspection ConstantConditions (str can really be null)
        if(str == null) {
            return new CommandFeedback(ListView.of());
        }

        if(input.base64Encode) {
            str = Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
        }

        final ResourcePath generatedPath = file.replaceLeafExtension(input.compiledFileNameSuffix);
        final HierarchicalResource generatedResource = resourceService.getHierarchicalResource(generatedPath);
        generatedResource.writeBytes(str.getBytes(StandardCharsets.UTF_8));
        context.provide(generatedResource, ResourceStampers.hashFile());

        //noinspection ConstantConditions (region may be null)
        return new CommandFeedback(ListView.of(CommandFeedbacks.showFile(generatedPath, null)));
    }

    @Override public Serializable key(Args input) {
        return input.file; // Task is keyed by file only.
    }
}
