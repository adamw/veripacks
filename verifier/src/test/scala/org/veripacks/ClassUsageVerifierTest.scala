package org.veripacks

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

class ClassUsageVerifierTest extends FlatSpec with ShouldMatchers {
  val pkg1 = Pkg("foo.bar.p1")
  val pkg1_1 = Pkg("foo.bar.p1.pp1")
  val pkg1_1_1 = Pkg("foo.bar.p1.pp1.ppp1")

  val pkg2 = Pkg("foo.bar.p2")

  val cls1_in_pkg1 = ClassName(pkg1, "cls1_in_pkg1")
  val cls2_in_pkg1 = ClassName(pkg1, "cls2_in_pkg1")
  val cls3_in_pkg1 = ClassName(pkg1, "cls3_in_pkg1")

  val cls1_in_pkg1_1 = ClassName(pkg1_1, "cls1_in_pkg1_1")
  val cls2_in_pkg1_1 = ClassName(pkg1_1, "cls2_in_pkg1_1")

  val cls1_in_pkg1_1_1 = ClassName(pkg1_1_1, "cls1_in_pkg1_1_1")
  val cls2_in_pkg1_1_1 = ClassName(pkg1_1_1, "cls2_in_pkg1_1_1")

  val cls1_in_pkg2 = ClassName(pkg2, "cls1_in_pkg2")

  val noExport = Map[Pkg, ExportDefinition]()

  val oneExport = Map[Pkg, ExportDefinition](pkg1 -> ExportClassesDefinition(Set(cls1_in_pkg1)))

  val allExport = Map[Pkg, ExportDefinition](pkg1 -> ExportAllDefinition)

  val packageAndSubpackageExports = Map[Pkg, ExportDefinition](
    pkg1 -> ExportClassesDefinition(Set(cls1_in_pkg1)),
    pkg1_1 -> ExportClassesDefinition(Set(cls1_in_pkg1_1)))

  val subSubPackageExports = Map[Pkg, ExportDefinition](pkg1_1_1 -> ExportClassesDefinition(Set(cls1_in_pkg1_1_1)))

  val testData = List[(String, Map[Pkg, ExportDefinition], ClassName, ClassName, Boolean)](
    ("allow using class in same package without access restrictions",
      noExport, cls1_in_pkg1, cls2_in_pkg1, true),
    ("allow using class in same package with access restrictions",
      oneExport, cls3_in_pkg1, cls2_in_pkg1, true),
    ("allow using class from parent package despite access restrictions",
      oneExport, cls3_in_pkg1, cls1_in_pkg1_1, true),
    ("allow using an exported class",
      oneExport, cls1_in_pkg1, cls1_in_pkg2, true),
    ("forbid using a not exported class",
      oneExport, cls2_in_pkg1, cls1_in_pkg2, false),
    ("allow using a class when all are exported",
      allExport, cls1_in_pkg1, cls1_in_pkg2, true),
    ("forbid using a not exported class, with multiple access definitions",
      packageAndSubpackageExports, cls2_in_pkg1_1, cls2_in_pkg1, false),
    ("forbid using a class which is visible only to intermediate packages",
      packageAndSubpackageExports, cls1_in_pkg1_1, cls1_in_pkg2, false),
    ("allow using a class which is visible only to intermediate packages, in the intermediate package",
      packageAndSubpackageExports, cls1_in_pkg1_1, cls2_in_pkg1, true),
    ("forbid using a not exported class from a sub-sub-package, even if the sub-package exports all",
      subSubPackageExports, cls2_in_pkg1_1_1, cls1_in_pkg1, false),
    ("allow using an exported class from a sub-sub-package if the sub-package exports all",
      subSubPackageExports, cls1_in_pkg1_1_1, cls1_in_pkg1, true)
  )

  for ((desc, accessDefinitions, clsUsed, clsUsedIn, expectedResult) <- testData) {
    it should desc in {
      val result = new ClassUsageVerifier(AccessDefinitions(accessDefinitions)).isAllowed(ClassUsage(clsUsed, clsUsedIn,
        MethodBodyUsageDetail("", "", 0)))

      result should be (expectedResult)
    }
  }
}
