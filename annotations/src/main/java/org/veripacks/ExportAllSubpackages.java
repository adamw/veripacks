package org.veripacks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
/**
 * Make all classes exported by subpackages accessible outside of this package. All classes which are not annotated
 * with @{@link Export} won't be accessible.
 */
public @interface ExportAllSubpackages {
}
