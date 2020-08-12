package mb.signature;

import mb.resource.ResourceKeyString;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;

public class SigClassloaderResources {
    public static ClassLoaderResourceRegistry createClassLoaderResourceRegistry() {
        return new ClassLoaderResourceRegistry(SigClassloaderResources.class.getClassLoader());
    }

    public static ClassLoaderResource createDefinitionDir(ClassLoaderResourceRegistry classLoaderResourceRegistry) {
        return classLoaderResourceRegistry.getResource(ResourceKeyString.of("mb/signature/"));
    }
}
