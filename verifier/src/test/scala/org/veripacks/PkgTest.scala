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
    (DefaultPkg("x.y"), DefaultPkg("x.y.z"), false),
    (DefaultPkg("x.y.w1"), DefaultPkg("x.y.w"), false)
  )

  for ((pkg1, pkg2, expectedResult) <- isSubpackageOfTestData) {
    it should s"return $expectedResult when calling $pkg1.isSubpckageOf($pkg2)" in {
      pkg1.isChildPackageOf(pkg2) should be (expectedResult)
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

  val allPkgsUpToCommonRootTestData = List(
    (Pkg("x.y.z"), RootPkg, Set(RootPkg, Pkg("x"), Pkg("x.y"), Pkg("x.y.z"))),
    (Pkg("w.x.y.z"), Pkg("w.x"), Set(Pkg("w.x"), Pkg("w.x.y"), Pkg("w.x.y.z"))),
    (Pkg("w.x.y.z"), Pkg("w.x.y1.z1"), Set(Pkg("w.x"), Pkg("w.x.y"), Pkg("w.x.y.z")))
  )

  for ((pkg, other, expectedResult) <- allPkgsUpToCommonRootTestData) {
    it should s"properly get all pkgs up to the common root of $pkg with $other" in {
      pkg.allPkgsUpToCommonRoot(other) should be (expectedResult)
    }
  }
}
