package mb.statix.multilang.eclipse;

import mb.statix.multilang.ContextConfig;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.MultiLangConfig;
import mb.statix.multilang.utils.ContextUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.metaborg.util.iterators.Iterables2;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class YamlContextMetadataProvider implements ContextMetadataProvider {
    @Override
    public Iterable<Map.Entry<ContextId, Supplier<Iterable<ContextConfig>>>> getContextConfigurations() {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        return Stream.of(projects)
            .map(project -> project.getFile("multilang.yaml"))
            .filter(IFile::exists)
            .flatMap(this::readFile)
            ::iterator;
    }

    private Stream<Map.Entry<ContextId, Supplier<Iterable<ContextConfig>>>> readFile(IFile configFile) {
        try {
            MultiLangConfig config = ContextUtils.readYamlConfig(configFile.getContents());
            return config.getCustomContexts().entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(new ContextId(entry.getKey()),
                    () -> Iterables2.singleton(entry.getValue())));
        } catch(CoreException e) {
            return Stream.empty();
        }
    }
}
