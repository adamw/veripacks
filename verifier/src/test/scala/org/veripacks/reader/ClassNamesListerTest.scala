package org.veripacks.reader

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.veripacks.{Pkg, ClassName}

class ClassNamesListerTest extends FlatSpec with ShouldMatchers {
  it should "list classes that are on the classpath in a folder" in {
    // When
    val result = new ClassNamesLister().list(Pkg("org.veripacks.data.accessdefinitions"))

    // Then
    result should have size (3)
    result.foreach(_.name should not include ("."))
    result should contain (ClassName(Pkg("org.veripacks.data.accessdefinitions"), "Cls1NoAnnotation"))
    result should contain (ClassName(Pkg("org.veripacks.data.accessdefinitions"), "package-info"))
  }

  it should "list classes that are on the classpath in a jar" in {
    // Given
    val pkg = Pkg("org.slf4j")

    // When
    val result = new ClassNamesLister().list(pkg)

    // Then
    result.foreach(_.pkg.name should startWith (pkg.name))
    result.foreach(_.name should not include ("."))
    result should contain (ClassName(Pkg("org.slf4j"), "Logger"))
    result should contain (ClassName(Pkg("org.slf4j.impl"), "StaticLoggerBinder"))
  }
}
