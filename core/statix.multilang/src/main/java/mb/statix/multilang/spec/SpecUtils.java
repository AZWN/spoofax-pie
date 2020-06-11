package mb.statix.multilang.spec;

import com.google.common.collect.Iterables;
import mb.nabl2.regexp.IAlphabet;
import mb.nabl2.regexp.impl.FiniteAlphabet;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.matching.TermMatch;
import mb.nabl2.terms.matching.TermMatch.IMatcher;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.resource.hierarchical.HierarchicalResource;
import mb.statix.spec.Rule;
import mb.statix.spec.RuleSet;
import mb.statix.spec.Spec;
import mb.statix.spoofax.StatixTerms;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

public class SpecUtils {

    public static SpecBuilder loadSpec(HierarchicalResource root, String initialModulePath, ITermFactory termFactory) throws IOException {
        StrategoTerms strategoTerms = new StrategoTerms(termFactory);
        ArrayList<String> loadedModules = new ArrayList<>();
        ArrayList<Module> fileSpecs = new ArrayList<>();
        Queue<String> modulesToLoad = new PriorityQueue<>();
        modulesToLoad.add(initialModulePath);

        while(!modulesToLoad.isEmpty()) {
            String currentModule = modulesToLoad.remove();
            // Load spec file content
            HierarchicalResource res = root.appendRelativePath(currentModule).appendExtensionToLeaf("spec.aterm");
            BufferedReader specReader = new BufferedReader(new InputStreamReader(res.openRead()));
            String specString = specReader.lines().collect(Collectors.joining("\n"));
            IStrategoTerm stxFileSpec = termFactory.parseFromString(specString);

            // Update pointers
            fileSpecs.add(Module.of(currentModule, strategoTerms.fromStratego(stxFileSpec)));
            loadedModules.add(currentModule);

            // Queue newly imported files
            IStrategoTerm imports = stxFileSpec.getSubterm(0);
            if(imports.getTermType() != IStrategoTerm.LIST) {
                throw new RuntimeException("Invalid spec file. Imports section should be a list, but was: " + imports);
            }
            imports.forEach(importDecl -> {
                if (!TermUtils.isString(importDecl)) {
                    throw new RuntimeException("Invalid file spec. Import module should be string, but was: " + importDecl);
                }
                String importedModule = ((IStrategoString) importDecl).stringValue();
                if (!loadedModules.contains(importedModule) && !modulesToLoad.contains(importedModule)) {
                    modulesToLoad.add(importedModule);
                }
            });
        }

        // Create builder for files
        return SpecBuilder.of(fileSpecs);
    }

    public static Spec mergeSpecs(Spec acc, Spec newSpec) {
        // Error when EOP is not equal, throw exception
        if (!acc.noRelationLabel().equals(newSpec.noRelationLabel())) {
            throw new RuntimeException("No relation labels do not match:" +
                acc.noRelationLabel() + " and " + newSpec.noRelationLabel());
        }

        // Cant merge rules & labels: manually create new collections
        ArrayList<Rule> rules = new ArrayList<>(acc.rules().getAllRules());
        rules.addAll(newSpec.rules().getAllRules());
        ArrayList<ITerm> labels = new ArrayList<>(acc.labels().symbols());
        labels.addAll(newSpec.labels().symbols());
        return Spec.builder().from(acc)
            .rules(RuleSet.of(rules))
            .addAllEdgeLabels(newSpec.edgeLabels())
            .addAllRelationLabels(newSpec.relationLabels())
            .labels(acc.labels())
            .putAllScopeExtensions(acc.scopeExtensions())
            .build();
    }

    public static IMatcher<Spec> fileSpec() {
        return TermMatch.M.appl6("FileSpec", TermMatch.M.list(), TermMatch.M.req(StatixTerms.labels()), TermMatch.M.req(StatixTerms.labels()), TermMatch.M.term(), StatixTerms.rules(), TermMatch.M.req(StatixTerms.scopeExtensions()),
            (t, l, edgeLabels, relationLabels, noRelationLabel, rules, ext) -> {
                final IAlphabet<ITerm> labels = new FiniteAlphabet<>(
                    Iterables2.cons(noRelationLabel, Iterables.concat(relationLabels, edgeLabels)));
                return Spec.of(rules, edgeLabels, relationLabels, noRelationLabel, labels, ext);
            });
    }
}
