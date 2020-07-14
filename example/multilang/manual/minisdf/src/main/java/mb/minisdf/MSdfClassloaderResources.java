package mb.minisdf;

import mb.resource.ResourceKeyString;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;

public class MSdfClassloaderResources {
    public static ClassLoaderResourceRegistry createClassLoaderResourceRegistry() {
        return new ClassLoaderResourceRegistry(MSdfClassloaderResources.class.getClassLoader());
    }

    public static ClassLoaderResource createDefinitionDir(ClassLoaderResourceRegistry classLoaderResourceRegistry) {
        return classLoaderResourceRegistry.getResource(ResourceKeyString.of("mb/minisdf/"));
    }
}
