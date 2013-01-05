veripacks - Verify Package Specifications
=========================================

What is it?
-----------

Veripacks implements some of the ideas from the blog post
["Let's turn packages into a module system"](http://www.warski.org/blog/2012/11/lets-turn-packages-into-a-module-system/).

Veripacks allows to specify which classes from a package should be accessible, and verify that the specification is met.

This is similar to package-private access in Java, however Veripacks extends this to subpackages, respecting package
parent-child dependencies. While usually the package is just a string identifier, Veripacks treats packages in a
hierarchical way. For example, `foo.bar.baz` is a subpackage of `foo.bar`. That means that exporting a class not only
hides other classes from the same package, but also classes from subpackages.

In some cases, Veripacks can be used to replace a separate build module. It aims to be a scalable and composable
solution, allowing for multi-layered exports, that is specifying access both for small pieces of code and large
functionalities.

Veripacks currently defines two annotations:
* `@Export` - applicable to a class, specifies that the annotated class should be visible to other packages. Classes
without the annotation but in the same package, and all classes in subpackages won't be visible. Several classes
in one package can be exported.
* `@ExportAll` - applicable to a package (in `package-info.java`), specifies that all classes and subpackages should be
exported. Has only documentational significance, as this is the default for all packages if no classes are explicitly
exported.

Access rules
------------

Access rules should be pretty straightforward and work "as expected".

More formally:
* subpackages can always access classes from parent packages
* within a package, all classes are visible
* otherwise a class `A` can be used in `B` if it is exported, looking from the package that is the closest parent of
both `A` and `B`

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
        .throwIfNotOk
    }

This will throw an exception if there are some specification violations. You can also inspect the result of the
`verify` call, which contains more detailed information (also included in the exception message).

The project files are deployed to SoftwareMill's public Nexus repository:

    <!-- Only the annotations -->
    <dependency>
        <groupId>org.veripacks</groupId>
        <artifactId>veripacks-annotations_2.10</artifactId>
        <version>0.1</version>
    </dependency>

    <!-- The verifier, has a dependency on the annotations -->
    <dependency>
        <groupId>org.veripacks</groupId>
        <artifactId>veripacks-verifier_2.10</artifactId>
        <version>0.1</version>
    </dependency>

    <repository>
        <id>SotwareMillPublicReleases</id>
        <name>SotwareMill Public Releases</name>
        <url>http://nexus.softwaremill.com/content/repositories/releases/</url>
    </repository>
    <repository>
        <id>SotwareMillPublicSnapshots</id>
        <name>SotwareMill Public Snapshots</name>
        <url>http://nexus.softwaremill.com/content/repositories/snapshots/</url>
    </repository>

What's next?
------------

* allow exporting none/some/all subpackages, along with exporting none/some/all classes
* add support for importing:
  * specify that a package can only be used if explicitly imported using a `@RequiresImport` annotation
  * support an `@Import` annotation to specify classes from which packages can be used in a package
* allow to specify which classes/subpackages are exported in a separate file

The last two points will allow to constrain usage of external libraries. For example, if using Hibernate, we could
specify that only classes from the `org.hibernate` package should be accessible, while classes from
`org.hibernate.internal` - not. Furthermore, by specifying that Hibernate needs to be explicitly imported, we could
verify that only packages that contain a `@Import("org.hibernate")` can access the Hibernate classes.

This is similar to creating a separate build-module and adding the Hibernate dependency to it only.

Notes
-----

Veripacks is also used to verify itself - the code contains some `@Export` annotations, usage of which is verified by
the single test in the `self-test` module.

Other tools, like Classycle or Structure 101 also allow similar verification to be done. Veripacks differs mainly by:
* the export/import metadata is kept close to the code itself, by using class/package annotations, instead of specifying
the metadata upfront in an "architecture" file or keeping it in an external file
* packages are treated in a hierarchical manner, with proper parent-child relationships

#### Version 0.1 (5 January 2013)

* Initial release
* Support for `@Export` and `@ExportAll` annotations