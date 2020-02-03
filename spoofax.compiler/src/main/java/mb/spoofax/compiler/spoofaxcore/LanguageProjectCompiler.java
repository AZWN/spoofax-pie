package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.StringUtil;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Value.Enclosing
public class LanguageProjectCompiler {
    private final TemplateWriter buildGradleTemplate;
    private final TemplateWriter generatedGradleTemplate;
    private final TemplateWriter packageInfoTemplate;

    private final ParserCompiler parserCompiler;
    private final StylerCompiler stylerCompiler;
    private final StrategoRuntimeCompiler strategoRuntimeCompiler;
    private final ConstraintAnalyzerCompiler constraintAnalyzerCompiler;

    public LanguageProjectCompiler(
        TemplateCompiler templateCompiler,
        ParserCompiler parserCompiler,
        StylerCompiler stylerCompiler,
        StrategoRuntimeCompiler strategoRuntimeCompiler,
        ConstraintAnalyzerCompiler constraintAnalyzerCompiler
    ) {
        this.buildGradleTemplate = templateCompiler.getOrCompileToWriter("language_project/build.gradle.kts.mustache");
        this.generatedGradleTemplate = templateCompiler.getOrCompileToWriter("language_project/generated.gradle.kts.mustache");
        this.packageInfoTemplate = templateCompiler.getOrCompileToWriter("language_project/package-info.java.mustache");

        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
    }

    public void generateInitial(Input input) throws IOException {
        buildGradleTemplate.write(input.buildGradleKtsFile(), input);
    }

    public void generateGradleFiles(Input input) throws IOException {
        final Shared shared = input.shared();

        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
        dependencies.add(GradleConfiguredDependency.api(shared.logApiDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.resourceDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.spoofaxCompilerInterfacesDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.commonDep()));
        dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
        parserCompiler.getLanguageProjectDependencies(input.parser()).addAllTo(dependencies);

        final ArrayList<String> copyResources = new ArrayList<>(input.additionalCopyResources());
        parserCompiler.getLanguageProjectCopyResources(input.parser()).addAllTo(copyResources);
        input.styler().ifPresent((i) -> {
            stylerCompiler.getLanguageProjectDependencies(i).addAllTo(dependencies);
            stylerCompiler.getLanguageProjectCopyResources(i).addAllTo(copyResources);
        });
        input.strategoRuntime().ifPresent((i) -> {
            strategoRuntimeCompiler.getLanguageProjectDependencies(i).addAllTo(dependencies);
            strategoRuntimeCompiler.getLanguageProjectCopyResources(i).addAllTo(copyResources);
        });
        input.constraintAnalyzer().ifPresent((i) -> {
            constraintAnalyzerCompiler.getLanguageProjectDependencies(i).addAllTo(dependencies);
            constraintAnalyzerCompiler.getLanguageProjectCopyResources(i).addAllTo(copyResources);
        });

        final HashMap<String, Object> map = new HashMap<>();
        final String languageDependencyCode = input.languageSpecificationDependency().caseOf()
            .project((projectPath) -> "createProjectDependency(\"" + projectPath + "\")")
            .module((coordinate) -> "createModuleDependency(\"" + coordinate.toGradleNotation() + "\")")
            .files((filePaths) -> "createFilesDependency(" + filePaths.stream().map((s) -> "\"" + s + "\"").collect(Collectors.joining(", ")) + ")");
        map.put("languageDependencyCode", languageDependencyCode);
        map.put("dependencyCodes", dependencies.stream().map(GradleConfiguredDependency::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));
        map.put("copyResourceCodes", copyResources.stream().map(StringUtil::doubleQuote).collect(Collectors.toCollection(ArrayList::new)));
        generatedGradleTemplate.write(input.generatedGradleKtsFile(), input, map);
    }

    public Output compile(Input input) throws IOException {
        final Shared shared = input.shared();

        // Class files
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        packageInfoTemplate.write(input.genPackageInfo().file(classesGenDirectory), input);

        // Files from other compilers.
        parserCompiler.compileLanguageProject(input.parser()).providedResources();
        try {
            input.styler().ifPresent((i) -> {
                try {
                    stylerCompiler.compileLanguageProject(i).providedResources();
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            input.strategoRuntime().ifPresent((i) -> {
                try {
                    strategoRuntimeCompiler.compileLanguageProject(i).providedFiles();
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            input.constraintAnalyzer().ifPresent((i) -> {
                try {
                    constraintAnalyzerCompiler.compileLanguageProject(i).providedResources();
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        return Output.builder().addAllProvidedFiles(input.providedFiles()).build();
    }

    // Inputs

    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends LanguageProjectCompilerData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        /// Project

        LanguageProject languageProject();


        /// Sub-inputs

        ParserCompiler.LanguageProjectInput parser();

        Optional<StylerCompiler.LanguageProjectInput> styler();

        Optional<StrategoRuntimeCompiler.LanguageProjectInput> strategoRuntime();

        Optional<ConstraintAnalyzerCompiler.LanguageProjectInput> constraintAnalyzer();


        /// Configuration

        GradleDependency languageSpecificationDependency();

        List<GradleConfiguredDependency> additionalDependencies();

        List<String> additionalCopyResources();


        /// Gradle files

        @Value.Default default ResourcePath buildGradleKtsFile() {
            return languageProject().project().baseDirectory().appendRelativePath("build.gradle.kts");
        }

        @Value.Default default ResourcePath generatedGradleKtsFile() {
            return languageProject().project().genSourceSpoofaxGradleDirectory().appendRelativePath("generated.gradle.kts");
        }

        default String relativeGeneratedGradleKtsFile() {
            final ResourcePath parentDirectory = Objects.requireNonNull(buildGradleKtsFile().getParent());
            return parentDirectory.relativizeToString(generatedGradleKtsFile());
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        default ResourcePath classesGenDirectory() {
            return languageProject().project().genSourceSpoofaxJavaDirectory();
        }


        /// Language project classes

        // package-info

        @Value.Default default TypeInfo genPackageInfo() {
            return TypeInfo.of(languageProject().packageId(), "package-info");
        }

        Optional<TypeInfo> manualPackageInfo();

        default TypeInfo packageInfo() {
            if(classKind().isManual() && manualPackageInfo().isPresent()) {
                return manualPackageInfo().get();
            }
            return genPackageInfo();
        }


        /// Provided files

        default ArrayList<ResourcePath> providedFiles() {
            final ArrayList<ResourcePath> generatedFiles = new ArrayList<>();
            if(classKind().isGenerating()) {
                generatedFiles.add(genPackageInfo().file(classesGenDirectory()));
            }
            parser().generatedFiles().addAllTo(generatedFiles);
            styler().ifPresent((i) -> i.generatedFiles().addAllTo(generatedFiles));
            strategoRuntime().ifPresent((i) -> i.providedFiles().addAllTo(generatedFiles));
            constraintAnalyzer().ifPresent((i) -> i.generatedFiles().addAllTo(generatedFiles));
            return generatedFiles;
        }


        /// Automatically provided sub-inputs

        Shared shared();


        // TODO: add check
    }

    @Value.Immutable
    public interface Output {
        class Builder extends LanguageProjectCompilerData.Output.Builder {}

        static Builder builder() {
            return new Builder();
        }

        List<ResourcePath> providedFiles();
    }
}
