package mb.spoofax.pie.benchmark.state.exec;

import kotlin.Unit;
import mb.fs.java.JavaFSPath;
import mb.pie.api.*;
import mb.pie.api.exec.BottomUpExecutor;
import mb.pie.api.exec.NullCancelled;
import mb.pie.api.fs.ResourceKt;
import mb.spoofax.pie.benchmark.state.*;
import mb.spoofax.pie.generated.processContainer;
import mb.spoofax.pie.generated.processDocumentWithText;
import mb.spoofax.pie.processing.ContainerResult;
import mb.spoofax.pie.processing.DocumentResult;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.stream.Collectors;

@State(Scope.Benchmark)
public class BUState {
    private SpoofaxPieState spoofaxPieState;
    private WorkspaceState workspaceState;
    private BottomUpExecutor executor;
    private HashMap<JavaFSPath, TaskKey> editorKeys = new HashMap<>();


    public void setup(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, InfraState infraState) {
        this.spoofaxPieState = spoofaxPieState;
        this.workspaceState = workspaceState;
        this.executor = infraState.pie.getBottomUpExecutor();
    }

    public void reset() {
        this.executor.dropObservers();
        this.editorKeys.clear();
    }


    /**
     * Adds a project, or updates a project, by setting the observer and then executing a project update in a top-down manner.
     */
    public void addProject(JavaFSPath project, Blackhole blackhole) {
        final Task<processContainer.Input, ContainerResult> task = spoofaxPieState.spoofaxPipeline.container(project, workspaceState.root);
        final TaskKey key = task.key();

        executor.setObserver(key, obj -> {
            blackhole.consume(obj);
            return Unit.INSTANCE;
        });

        try {
            blackhole.consume(executor.requireTopDown(task, new NullCancelled()));
        } catch(ExecException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes a project, removing the observer for that project.
     */
    public void removeProject(JavaFSPath project) {
        final Task<processContainer.Input, ContainerResult> task = spoofaxPieState.spoofaxPipeline.container(project, workspaceState.root);
        final TaskKey key = task.key();
        executor.removeObserver(key);
    }


    /**
     * Adds an editor, or updates an existing editor, by setting the observer and then executing an editor update in a top-down manner.
     */
    public void addOrUpdateEditor(String text, JavaFSPath file, JavaFSPath project, Blackhole blackhole) {
        final Task<processDocumentWithText.Input, DocumentResult> task =
            spoofaxPieState.spoofaxPipeline.documentWithText(file, project, workspaceState.root, text);
        final TaskKey key = task.key();
        editorKeys.put(file, key);

        executor.setObserver(key, obj -> {
            blackhole.consume(obj);
            return Unit.INSTANCE;
        });

        try {
            blackhole.consume(executor.requireTopDown(task, new NullCancelled()));
        } catch(ExecException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes an open editor, removing the observer for that editor.
     */
    public void removeEditor(JavaFSPath file) {
        final TaskKey key = editorKeys.get(file);
        if(key != null) {
            executor.removeObserver(key);
        }
    }


    /**
     * Executes the pipeline in a bottom-up way, with given changed paths.
     */
    public void execPathChanges(Set<JavaFSPath> changedPaths) {
        final HashSet<ResourceKey> changedResources =
            changedPaths.stream().map(ResourceKt::toResourceKey).collect(Collectors.toCollection(HashSet::new));
        try {
            executor.requireBottomUp(changedResources, new NullCancelled());
        } catch(ExecException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
