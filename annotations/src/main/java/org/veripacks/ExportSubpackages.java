package org.veripacks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
/**
 * Make classes exported by the given subpackages accessible outside of this package. All classes which are not
 * annotated with @{@link Export}, as well as classes from other subpackages, won't be accessible.
 */
public @interface ExportSubpackages {
    /**
     * @return Names of direct subpackages to export. E.g. to export the "foo.bar.baz" subpackage from "foo.bar",
     * the value should be "baz".
     */
    String[]    value()  default {};
}
