package org.veripacks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
/**
 * Make all classes and all classes exported by subpackages accessible outside of this package.
 *
 * This is only a marker annotation, as by default, if no classes or subpackages are explicitly exported, everything
 * is exported.
 */
public @interface ExportAll {
}
