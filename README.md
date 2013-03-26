veripacks - Verify Package Specifications
=========================================

What is it?
-----------

Veripacks implements some of the ideas from the blog post
["Let's turn packages into a module system"](http://www.warski.org/blog/2012/11/lets-turn-packages-into-a-module-system/).

Veripacks allows to specify which classes from a package hierarchy should be accessible, and verify that the
specification is met.

This is similar to package-private access in Java, however Veripacks extends this to subpackages, respecting package
parent-child dependencies. While usually the package is just a string identifier, Veripacks treats packages in a
hierarchical way. For example, `foo.bar.baz` is a subpackage of `foo.bar`. That means that exporting a class not only
hides other classes from the same package, but also classes from subpackages.

In some cases, Veripacks can be used to replace a separate build module. It aims to be a scalable and composable
solution, allowing for multi-layered exports, that is specifying access both for small pieces of code and large
functionalities.

Veripacks currently defines the following annotations:
* `@Export` - applicable to a class, specifies that the annotated class should be visible to other packages. Classes
without the annotation but in the same package, and all classes in non-exported subpackages won't be visible.
Several classes in one package can be exported.
* `@ExportSubpackages` - applicable to a package (in `package-info.java`), allows to specify which subpackages should
be exported. Only classes/sub-subpackages exported by the subpackage will be exported by the package. The value of the
annotation is an array of immediate subpackage names.
* `@ExportAllClasses` and `@ExportAllSubpackages` - applicable to a package
* `@ExportAll` - applicable to a package, specifies that all classes and subpackages should be exported. Has only
documentational significance, as this is the default for all packages if no classes are explicitly exported.
* `@RequiresImport` - specifies that exported classes from this package (and any child packages) can only be used, if
the package is explicitly import using `@Import`.
* `@Import` - imports a package annotated with `@RequiresImport`. Exported classes from the package may be used in
the annotated package and any child packages.

Using an export-all annotation and an export-specific annotation for same element type (class, subpackage) will result
in an error.

Access rules
------------

Access rules should be pretty straightforward and work "as expected".

More formally:
* packages can always access classes from parent packages
* within a package, all classes are visible
* otherwise a class `A` can be used in `B` if:
  * it is exported, looking from the package that is the closest parent of both `A` and `B` (closest common root)
  * all packages between the closest common root and `B`'s package that require import, must be imported
by `A`'s package or some parent package.

Example
-------

Using a bit of an imaginary syntax to make things compact:

    package foo.bar.p1 {
      @Export
      class A { ... }

      class B { ... }
    }

    package foo.bar.p1.sub_p1 {
      class C { ... }
    }

    package foo.bar.p2 {
      class Test {
        // ok, A is exported
        new A()

        // illegal, B is not exported
        new B()

        // illegal, C is in a subpackage of p1, and p1 only exports A
        new C()
      }
    }

How to use it?
--------------

Veripacks can be used with any language running on the JVM; while written in Scala, it will work without problems
in Java-only projects.

No build plugins or such are needed; just create a new test, with the following body:

    public void runVeripacksTest() {
      new Verifier()
        .verify("foo.bar")
        .throwIfNotOk()
    }

This will throw an exception if there are some specification violations. You can also inspect the result of the
`verify` call, which contains more detailed information (also included in the exception message).

The project files are deployed to Sonatype's OSS public Nexus repository, which is synced to Maven Central:

    <!-- Only the annotations -->
    <dependency>
        <groupId>org.veripacks</groupId>
        <artifactId>veripacks-annotations_2.10</artifactId>
        <version>0.3</version>
    </dependency>

    <!-- The verifier, has a dependency on the annotations -->
    <dependency>
        <groupId>org.veripacks</groupId>
        <artifactId>veripacks-verifier_2.10</artifactId>
        <version>0.3</version>
        <scope>test</scope>
    </dependency>

What's next?
------------

* extend `@Import` to work with third-party packages (libraries)
* allow to specify which classes/subpackages are exported in a separate file
* IDE support

The first two points will allow to constrain usage of external libraries. For example, if using Hibernate, we could
specify that only classes from the `org.hibernate` package should be accessible, while classes from
`org.hibernate.internal` - not. Furthermore, by specifying that Hibernate needs to be explicitly imported, we could
verify that only packages that contain a `@Import("org.hibernate")` can access the Hibernate classes.

This is similar to creating a separate build-module and adding the Hibernate dependency to it only.

Sub- and child- packages
------------------------

Vocabulary:
* subpackage - immediate child of a package. E.g. `com.foo.bar` is a subpackage of `com.foo`, but `com.foo.bar.baz`
isn't.
* child package - any child of a package. Following the example above, both packages are child packages of `com.foo`.

Blogs about Veripacks
---------------------

* [How to replace a build module with Veripacks](http://www.warski.org/blog/2013/03/how-to-replace-a-build-module-with-veripacks/)
* [Veripacks 0.3: importing packages (transitively, of course)](http://www.warski.org/blog/2013/03/veripacks-0-3-importing-packages-transitively-of-course/)
* [Veripacks 0.2: exporting subpackages](http://www.warski.org/blog/2013/02/veripacks-0-2-exporting-subpackages/)
* [Veripacks 0.1 – Verify Package Specifications](http://www.warski.org/blog/2013/01/veripacks-0-1-verify-package-specifications/)

Notes
-----

Veripacks is also used to verify itself - the code contains some `@Export` annotations, usage of which is verified by
the single test in the `self-test` module.

Other tools, like Classycle or Structure 101 also allow similar verification to be done. Veripacks differs mainly by:
* the export/import metadata is kept close to the code itself, by using class/package annotations, instead of specifying
the metadata upfront in an "architecture" file or keeping it in an external file
* packages are treated in a hierarchical manner, with proper parent-child relationships

Licensed under Apache2.

#### Version 0.3 (6 March 2013)

* `@Import`, `@RequiresImport` annotations

#### Version 0.2 (3 February 2013)

* Support for exporting subpackages (`@ExportSubpackages`)
* `@ExportAllClasses`, `@ExportAllSubpackages` annotations

#### Version 0.1 (5 January 2013)

* Initial release
* Support for `@Export` and `@ExportAll` annotations
