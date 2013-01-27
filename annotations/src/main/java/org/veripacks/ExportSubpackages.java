package org.veripacks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface ExportSubpackages {
    /**
     * @return Names of direct subpackages to export. E.g. to export the "foo.bar.baz" subpackage from "foo.bar",
     * the value should be "baz".
     */
    String[]    value()  default {};
}
