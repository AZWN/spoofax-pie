package mb.ministr;

import mb.resource.ResourceKeyString;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;

public class MStrClassloaderResources {
    public static ClassLoaderResourceRegistry createClassLoaderResourceRegistry() {
        return new ClassLoaderResourceRegistry(MStrClassloaderResources.class.getClassLoader());
    }

    public static ClassLoaderResource createDefinitionDir(ClassLoaderResourceRegistry classLoaderResourceRegistry) {
        return classLoaderResourceRegistry.getResource(ResourceKeyString.of("mb/ministr/"));
    }

    public static ClassLoaderResource defaultDefinitionDir() {
        return createDefinitionDir(createClassLoaderResourceRegistry());
    }
}
