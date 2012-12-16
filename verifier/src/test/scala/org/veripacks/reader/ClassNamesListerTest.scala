package org.veripacks.reader

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.veripacks.ClassName

class ClassNamesListerTest extends FlatSpec with ShouldMatchers {
  it should "list classes that are on the classpath in a folder" in {
    // When
    val result = new ClassNamesLister().list("org.veripacks.data.t1")

    // Then
    result should have size (4)
    result should contain (ClassName("org.veripacks.data.t1.p11.Class112"))
    result should contain (ClassName("org.veripacks.data.t1.p12.package-info"))
  }

  it should "list classes that are on the classpath in a jar" in {
    // Given
    val pkg = "org.slf4j"

    // When
    val result = new ClassNamesLister().list(pkg)

    // Then
    result.foreach(_.name should startWith (pkg))
    result should contain (ClassName("org.slf4j.Logger"))
    result should contain (ClassName("org.slf4j.impl.StaticLoggerBinder"))
  }
}
