package mb.spoofax.compiler.spoofaxcore;

import mb.common.util.ListView;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class MultilangAnalyzerCompiler {

    private final TemplateWriter analyzeProjectTemplate;
    private final TemplateWriter indexAstTaskDefTemplate;
    private final TemplateWriter preStatixTaskDefTemplate;
    private final TemplateWriter postStatixTaskDefTemplate;

    public MultilangAnalyzerCompiler(TemplateCompiler templateCompiler) {
        this.analyzeProjectTemplate = templateCompiler.getOrCompileToWriter("multilang_analyzer/AnalyzeProjectTaskDef.java.mustache");
        this.indexAstTaskDefTemplate = templateCompiler.getOrCompileToWriter("multilang_analyzer/IndexAstTaskDef.java.mustache");
        this.preStatixTaskDefTemplate = templateCompiler.getOrCompileToWriter("multilang_analyzer/PreStatixTaskDef.java.mustache");
        this.postStatixTaskDefTemplate = templateCompiler.getOrCompileToWriter("multilang_analyzer/PostStatixTaskDef.java.mustache");
    }

    // Language project

    public ListView<GradleConfiguredDependency> getLanguageProjectDependencies(LanguageProjectInput input) {
        return ListView.of(GradleConfiguredDependency.api(input.shared().constraintCommonDep()));
    }

    public ListView<String> getLanguageProjectCopyResources(LanguageProjectInput input) {
        return ListView.of();
    }

    public Output compileLanguageProject(LanguageProjectInput input) {
        // There is only compilation output in the adapter project, so return empty result here.
        return Output.builder().build();
    }

    // Adapter project

    public Output compileAdapterProject(AdapterProjectInput input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManualOnly()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        outputBuilder.addProvidedResources(
            analyzeProjectTemplate.write(input.genAnalyzeTaskDef().file(classesGenDirectory), input),
            indexAstTaskDefTemplate.write(input.genIndexAstTaskDef().file(classesGenDirectory), input),
            preStatixTaskDefTemplate.write(input.genPreStatixTaskDef().file(classesGenDirectory), input),
            postStatixTaskDefTemplate.write(input.genPostStatixTaskDef().file(classesGenDirectory), input)
        );
        return outputBuilder.build();
    }

    // Inputs

    @Value.Immutable
    public interface LanguageProjectInput extends Serializable {
        class Builder extends MultilangAnalyzerCompilerData.LanguageProjectInput.Builder {}

        static Builder builder() {
            return new Builder();
        }

        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        @Value.Derived default ResourcePath classesGenDirectory() {
            return languageProject().project().genSourceSpoofaxJavaDirectory();
        }

        // List of all provided files

        default ListView<ResourcePath> providedFiles() {
            return ListView.of();
        }

        /// Automatically provided sub-inputs

        Shared shared();

        LanguageProject languageProject();
    }

    @Value.Immutable
    public interface AdapterProjectInput extends Serializable {
        class Builder extends MultilangAnalyzerCompilerData.AdapterProjectInput.Builder {}

        static Builder builder() {
            return new Builder();
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Classes

        @Value.Derived default ResourcePath classesGenDirectory() {
            return adapterProject().project().genSourceSpoofaxJavaDirectory();
        }

        // Analyze

        @Value.Default default TypeInfo genAnalyzeTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "AnalyzeProject");
        }

        Optional<TypeInfo> manualAnalyzeTaskDef();

        default TypeInfo analyzeTaskDef() {
            if(classKind().isManual() && manualAnalyzeTaskDef().isPresent()) {
                return manualAnalyzeTaskDef().get();
            }
            return genAnalyzeTaskDef();
        }

        // Transformation tasks

        @Value.Default default TypeInfo genIndexAstTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "IndexAst");
        }

        Optional<TypeInfo> manualIndexAstTaskDef();

        default TypeInfo indexAstTaskDef() {
            if(classKind().isManual() && manualIndexAstTaskDef().isPresent()) {
                return manualIndexAstTaskDef().get();
            }
            return genIndexAstTaskDef();
        }

        @Value.Default default TypeInfo genPreStatixTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "PreStatix");
        }

        Optional<TypeInfo> manualPreStatixTaskDef();

        default TypeInfo preStatixTaskDef() {
            if(classKind().isManual() && manualPreStatixTaskDef().isPresent()) {
                return manualPreStatixTaskDef().get();
            }
            return genPreStatixTaskDef();
        }

        @Value.Default default TypeInfo genPostStatixTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "PostStatix");
        }

        Optional<TypeInfo> manualPostStatixTaskDef();

        default TypeInfo postStatixTaskDef() {
            if(classKind().isManual() && manualPostStatixTaskDef().isPresent()) {
                return manualPostStatixTaskDef().get();
            }
            return genPostStatixTaskDef();
        }

        // Transformation settings

        @Value.Default default String preAnalysisStrategy() {
            return "pre";
        }

        @Value.Default default String postAnalysisStrategy() {
            return "post";
        }

        // Identifiers

        @Value.Default default String languageId() {
            return shared().defaultBasePackageId();
        }

        @Value.Default default String contextId() {
            return shared().defaultBasePackageId();
        }


        // List of all generated files

        default ListView<ResourcePath> generatedFiles() {
            if(classKind().isManualOnly()) {
                return ListView.of();
            }
            return ListView.of(
                genAnalyzeTaskDef().file(classesGenDirectory()),
                genIndexAstTaskDef().file(classesGenDirectory()),
                genPreStatixTaskDef().file(classesGenDirectory()),
                genPostStatixTaskDef().file(classesGenDirectory())
            );
        }


        /// Automatically provided sub-inputs

        Shared shared();

        AdapterProject adapterProject();

        LanguageProjectInput languageProjectInput();
    }

    @Value.Immutable
    public interface Output {
        class Builder extends MultilangAnalyzerCompilerData.Output.Builder {}

        static Builder builder() {
            return new Output.Builder();
        }

        List<HierarchicalResource> providedResources();
    }
}

