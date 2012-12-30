package org.veripacks

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class ClassNameTest extends FlatSpec with ShouldMatchers {
  val fromDottedNameTestData = List(
    ("SomeClass", ClassName(RootPkg, "SomeClass")),
    ("foo.bar.SomeClass", ClassName(DefaultPkg("foo.bar"), "SomeClass"))
  )

  for ((className, expectedResult) <- fromDottedNameTestData) {
    it should s"parse $className to $expectedResult" in {
      ClassName.fromDottedName(className) should be (expectedResult)
    }
  }
}
