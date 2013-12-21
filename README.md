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
* `@NotVerified` - classes annotated with that annotation won't be verified by Veripacks. Useful e.g. for classes which
wire objects from various packages together ("manual DI").

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

    def runVeripacksTest() {
      VeripacksBuilder.build
        .verify("foo.bar")
        .throwIfNotOk()
    }

(Note: when using Veripacks from Java, the above becomes:

    public void runVeripacksTest() {
      VeripacksBuilder$.MODULE$.build()
        .verify("foo.bar")
        .throwIfNotOk();
    }

as that's how a Scala `object` is translated to Java.)

This will throw an exception if there are some specification violations. You can also inspect the result of the
`verify` call, which contains more detailed information (also included in the exception message).

The project files are deployed to Sonatype's OSS public Nexus repository, which is synced to Maven Central:

    <!-- Only the annotations -->
    <dependency>
        <groupId>org.veripacks</groupId>
        <artifactId>veripacks-annotations_2.10</artifactId>
        <version>0.4</version>
    </dependency>

    <!-- The verifier, has a dependency on the annotations -->
    <dependency>
        <groupId>org.veripacks</groupId>
        <artifactId>veripacks-verifier_2.10</artifactId>
        <version>0.4</version>
        <scope>test</scope>
    </dependency>

Requiring import for 3rd party libraries
----------------------------------------

It is also possible to constrain the usage of 3rd party packages using Veripacks. When a `Veripacks` instance is
constructed, it is possible to specify additional packages for which import is required:

````scala
VeripacksBuilder
    .requireImportOf("org.hibernate")
    .requireImportOf("com.softwaremill")
    .build
    .verify("com.company.project")
    .throwIfNotOk()
````

Then if a Hibernate class is used in a class in the `com.company.project` package (or a child package), its usage
is verified. That is, some package must contain the `@Import("org.hibernate")` annotation (the import may be of any
child package, so when using only commons, `@Import("org.hibernate.common")` will work as well).

The package names are checked by prefix, so to check usages of all `com` packages, just invoke
`requireImportOf("com")`. To exclude a class, invoke `doNotRequireImportOf("com.softwaremill")` before including
`"com"`. Filters defined earlier have precedence.

This is similar to creating a separate build-module and adding the Hibernate dependency to it only.

Specifying a custom metadata reader
-----------------------------------

Sometimes it may be desirable to specify a custom metadata reader. For example, if the package naming convention in a
project is `com.<company>.<project>.<main module>.<submodule>`, instead of adding an `@RequiresImport` annotation to
each main-module package, it would be better to automatically require import for such packages.

We may achieve this by extending the `CustomAccessDefinitionsReader` trait and providing it when building the
`Veripacks` instance:

````scala
VeripacksBuilder
  .withCustomAccessDefinitionReader(new CustomAccessDefinitionsReader {
    override def isRequiresImport(pkg: Pkg) = pkg.name.split(".").length == 4
  })
  .build
  .verify(List("com.<company>.<project>"))
  .throwIfNotOk()
````

What's next?
------------

* allow to specify which classes/subpackages are exported in a separate file
* IDE support

The first point will allow to further constrain usage of external libraries. For example, if using Hibernate, we could
specify that only classes from the `org.hibernate` package should be accessible, while classes from
`org.hibernate.internal` - not.

Sub- and child- packages
------------------------

Vocabulary:
* subpackage - immediate child of a package. E.g. `com.foo.bar` is a subpackage of `com.foo`, but `com.foo.bar.baz`
isn't.
* child package - any child of a package. Following the example above, both packages are child packages of `com.foo`.

Blogs about Veripacks
---------------------

* [Verifying usage of 3rd party libraries using Veripacks](http://www.warski.org/blog/2013/08/verifying-usage-of-3rd-party-libraries-using-veripacks/)
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

#### Version 0.4 (5 August 2013)

* support for 3rd party libraries imports
* `@NotVerified` annotation
* wireable code, overridable components

#### Version 0.3 (6 March 2013)

* `@Import`, `@RequiresImport` annotations

#### Version 0.2 (3 February 2013)

* Support for exporting subpackages (`@ExportSubpackages`)
* `@ExportAllClasses`, `@ExportAllSubpackages` annotations

#### Version 0.1 (5 January 2013)

* Initial release
* Support for `@Export` and `@ExportAll` annotations


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/adamw/veripacks/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

