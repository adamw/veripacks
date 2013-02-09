package org.veripacks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
/**
 * Make all classes from this package accessible outside of this package (the classes are still visible in child
 * packages).
 */
public @interface ExportAllClasses {
}
