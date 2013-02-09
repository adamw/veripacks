package org.veripacks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
/**
 * Makes classes exported by packages, which require importing, accessible.
 *
 * Each imported package must have an @{@link RequiresImport} annotation.
 */
public @interface Import {
    /**
     * @return Full names of packages to import.
     */
    String[]    value()  default {};
}
