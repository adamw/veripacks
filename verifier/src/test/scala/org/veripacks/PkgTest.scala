package org.veripacks

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class PkgTest extends FlatSpec with ShouldMatchers {
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

  val parentTestData = List(
    (RootPkg, None),
    (DefaultPkg("x"), Some(RootPkg)),
    (DefaultPkg("x.y"), Some(DefaultPkg("x"))),
    (DefaultPkg("x.y.z"), Some(DefaultPkg("x.y")))
  )

  for ((pkg, expectedResult) <- parentTestData) {
    it should s"properly get the parent of $pkg" in {
      pkg.parent should be (expectedResult)
    }
  }
}
