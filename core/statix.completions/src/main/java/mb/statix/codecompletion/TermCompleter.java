package mb.statix.codecompletion;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.statix.common.SolverContext;
import mb.statix.common.SolverState;
import mb.statix.common.strategies.Strategy;
import mb.statix.constraints.CResolveQuery;
import mb.statix.constraints.CUser;
import mb.statix.search.FocusedSolverState;
import mb.statix.search.strategies.LimitStrategy;
import mb.statix.search.strategies.Strategies;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mb.nabl2.terms.build.TermBuild.B;
import static mb.statix.search.strategies.SearchStrategies.*;
import static mb.statix.search.strategies.Strategies.*;


/**
 * The term completer.
 */
public final class TermCompleter {

    private static Strategy<FocusedSolverState<CUser>, SolverState, SolverContext> completionStrategyCont =
        // @formatter:off
        seq(expandRule())
         .$(infer())
         .$(isSuccessful())
         .$(delayStuckQueries())
         .$(repeat(seq(limit(1, focus(CResolveQuery.class)))
            .$(expandQuery())
            .$(infer())
            .$(isSuccessful())
            .$(delayStuckQueries())
            .$()
         ))
         .$();
    // @formatter:on

//    private static Strategy<SolverState, SolverState, SolverContext> completionStrategy =
//    // @formatter:off
//        seq(Strategies.<SolverState, SolverContext>id())
//         .$(seq(limit(1, focus(CUser.class)))
//             .$(expandRule())
//             .$(infer())
//             .$(isSuccessful())
//             .$(delayStuckQueries())
//             .$())
//         .$(repeat(seq(limit(1, focus(CResolveQuery.class)))
//            .$(expandQuery())
//            .$(infer())
//            .$(isSuccessful())
//            .$(delayStuckQueries())
//            .$()
//         ))
//         .$();
//    // @formatter:on

    /**
     * Completes the specified constraint.
     *
     * @param ctx the search context
     * @param state the initial search state
     * @param placeholderVar the var of the placeholder to complete
     * @return the resulting completion proposals
     */
    public List<CompletionSolverProposal> complete(SolverContext ctx, SolverState state, ITermVar placeholderVar) throws InterruptedException {
        return completeNodes(ctx, state, placeholderVar).map(s -> new CompletionSolverProposal(s, project(placeholderVar, s))).collect(Collectors.toList());
    }

    /**
     * Completes the specified constraint.
     *
     * @param ctx the search context
     * @param state the initial search state
     * @param placeholderVar the var of the placeholder to complete
     * @return the resulting states
     */
    public Stream<SolverState> completeNodes(SolverContext ctx, SolverState state, ITermVar placeholderVar) throws InterruptedException {
        return buildCompletionStrategy(placeholderVar, completionStrategyCont).apply(ctx, state);
    }

    private Strategy<SolverState, SolverState, SolverContext> buildCompletionStrategy(ITermVar placeholderVar, Strategy<FocusedSolverState<CUser>, SolverState, SolverContext> continuation) {
        return seq(limit(1, focus(CUser.class, c -> c.args().stream().anyMatch(a -> a.getVars().contains(placeholderVar)))))
            .$(continuation)
            .$();
    }

    private static ITerm project(ITermVar placeholderVar, SolverState s) {
        return s.getState().unifier().findRecursive(placeholderVar);
//        if(s.getExistentials() != null && s.getExistentials().containsKey(placeholderVar)) {
//            return s.getState().unifier().findRecursive(s.getExistentials().get(placeholderVar));
//        } else {
//            return placeholderVar;
//        }
    }

    /**
     * A completion solver result.
     */
    public final static class CompletionSolverProposal {
        private final SolverState newState;
        private final ITerm term;

        public CompletionSolverProposal(SolverState newState, ITerm term) {
            this.newState = newState;
            this.term = term;
        }

        public SolverState getNewState() {
            return newState;
        }

        public ITerm getTerm() {
            return term;
        }
    }
}
