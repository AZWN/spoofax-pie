// @formatter:off
@Value.Style(
    get = { "is*", "get*" },
    // prevent generation of javax.annotation.*; bogus entry, because empty list = allow all
    allowedClasspathAnnotations = {Override.class}
)
// @formatter:on
package mb.statix.multilang;

import org.immutables.value.Value;
