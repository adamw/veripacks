package org.veripacks

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class ModelTest extends FlatSpec with ShouldMatchers {
  val isSubpackageOfTestData = List(
    (RootPkg, RootPkg, true),
    (RootPkg, DefaultPkg("x.y"), false),
    (DefaultPkg("x.y"), RootPkg, true),
    (DefaultPkg("x.y"), DefaultPkg("x.y"), true),
    (DefaultPkg("x.y.z"), DefaultPkg("x.y"), true),
    (DefaultPkg("x.y"), DefaultPkg("x.y.z"), false)
  )

  for ((pkg1, pkg2, expectedResult) <- isSubpackageOfTestData) {
    it should s"return $expectedResult when calling $pkg1.isSubpckageOf($pkg2)" in {
      pkg1.isSubpackageOf(pkg2) should be (expectedResult)
    }
  }

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
