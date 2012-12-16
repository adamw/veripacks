package org.veripacks.reader

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.veripacks.{DefaultPkg, ClassUsage, ClassName}

class ClassUsagesReaderTest extends FlatSpec with ShouldMatchers {
  it should "read usages" in {
    // Given
    val rootPkg = DefaultPkg("org.veripacks.data.t1")

    val cls111 = ClassName(rootPkg.child("p11"), "Class111")
    val cls112 = ClassName(rootPkg.child("p11"), "Class112")
    val cls121 = ClassName(rootPkg.child("p12"), "Class121")

    // When
    val result = new ClassUsagesReader().read(cls121)

    // Then
    result should be (List(
      ClassUsage(cls111, cls121, 6),
      ClassUsage(cls112, cls121, 7)
    ))
  }
}
