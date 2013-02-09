package org.veripacks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
/**
 * Exported classes from this package can be only accessed if this package is explicitly imported using @{@link Import}.
 */
public @interface RequireImport {
}
