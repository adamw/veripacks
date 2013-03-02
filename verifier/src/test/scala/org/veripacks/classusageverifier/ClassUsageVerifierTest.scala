package org.veripacks.classusageverifier

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.veripacks._

class ClassUsageVerifierTest extends FlatSpec with ShouldMatchers {
  val pkg1 = Pkg("foo.bar.p1")
  val pkg1_1 = Pkg("foo.bar.p1.pp1")
  val pkg1_1_1 = Pkg("foo.bar.p1.pp1.ppp1")
  val pkg1_1_2 = Pkg("foo.bar.p1.pp1.ppp2")

  val pkg2 = Pkg("foo.bar.p2")

  val cls1_in_pkg1 = ClassName(pkg1, "cls1_in_pkg1")
  val cls2_in_pkg1 = ClassName(pkg1, "cls2_in_pkg1")
  val cls3_in_pkg1 = ClassName(pkg1, "cls3_in_pkg1")

  val cls1_in_pkg1_1 = ClassName(pkg1_1, "cls1_in_pkg1_1")
  val cls2_in_pkg1_1 = ClassName(pkg1_1, "cls2_in_pkg1_1")

  val cls1_in_pkg1_1_1 = ClassName(pkg1_1_1, "cls1_in_pkg1_1_1")
  val cls2_in_pkg1_1_1 = ClassName(pkg1_1_1, "cls2_in_pkg1_1_1")

  val cls1_in_pkg1_1_2 = ClassName(pkg1_1_2, "cls2_in_pkg1_1_2")

  val cls1_in_pkg2 = ClassName(pkg2, "cls1_in_pkg2")

  val noExport = Map[Pkg, ExportDef]()

  val oneClassExport = Map[Pkg, ExportDef](pkg1 -> ExportDef(ExportSpecificClassesDef(Set(cls1_in_pkg1))))

  val allExport = Map[Pkg, ExportDef](pkg1 -> ExportDef.All)

  val packageAndSubpackageClassExports = Map[Pkg, ExportDef](
    pkg1 -> ExportDef(ExportSpecificClassesDef(Set(cls1_in_pkg1))),
    pkg1_1 -> ExportDef(ExportSpecificClassesDef(Set(cls1_in_pkg1_1))))

  val subSubPackageClassExports = Map[Pkg, ExportDef](pkg1_1_1 -> ExportDef(ExportSpecificClassesDef(Set(cls1_in_pkg1_1_1))))

  val subSubPackageClassAndSubPackageExports1 = Map[Pkg, ExportDef](
    pkg1_1_1 -> ExportDef(ExportSpecificClassesDef(Set(cls1_in_pkg1_1_1))),
    pkg1_1 -> ExportDef(ExportSpecificPkgsDef(Set(pkg1_1_1))))

  val subSubPackageClassAndSubPackageExports2 = Map[Pkg, ExportDef](
    pkg1_1_1 -> ExportDef(ExportSpecificClassesDef(Set(cls1_in_pkg1_1_1))),
    pkg1_1 -> ExportDef(ExportSpecificPkgsDef(Set(pkg1_1_2))))

  val testData = List[(String, Map[Pkg, ExportDef], ClassName, ClassName, Boolean)](
    ("allow using class in same package without access restrictions",
      noExport, cls1_in_pkg1, cls2_in_pkg1, true),
    ("allow using class in same package with access restrictions",
      oneClassExport, cls3_in_pkg1, cls2_in_pkg1, true),
    ("allow using class from parent package despite access restrictions",
      oneClassExport, cls3_in_pkg1, cls1_in_pkg1_1, true),
    ("allow using an exported class",
      oneClassExport, cls1_in_pkg1, cls1_in_pkg2, true),
    ("forbid using a not exported class",
      oneClassExport, cls2_in_pkg1, cls1_in_pkg2, false),
    ("allow using a class when all are exported",
      allExport, cls1_in_pkg1, cls1_in_pkg2, true),
    ("forbid using a not exported class, with multiple access definitions",
      packageAndSubpackageClassExports, cls2_in_pkg1_1, cls2_in_pkg1, false),
    ("forbid using a class which is visible only to intermediate packages",
      packageAndSubpackageClassExports, cls1_in_pkg1_1, cls1_in_pkg2, false),
    ("allow using a class which is visible only to intermediate packages, in the intermediate package",
      packageAndSubpackageClassExports, cls1_in_pkg1_1, cls2_in_pkg1, true),
    ("forbid using a not exported class from a sub-sub-package, even if the sub-package exports all",
      subSubPackageClassExports, cls2_in_pkg1_1_1, cls1_in_pkg1, false),
    ("allow using an exported class from a sub-sub-package if the sub-package exports all",
      subSubPackageClassExports, cls1_in_pkg1_1_1, cls1_in_pkg1, true),
    ("allow using a class exported from a sub-sub-package, if the sub-sub-package is exported by the sub-package",
      subSubPackageClassAndSubPackageExports1, cls1_in_pkg1_1_1, cls1_in_pkg1, true),
    ("forbid using a class exported from a sub-sub-package, if the sub-sub-package is not exported by the sub-package",
      subSubPackageClassAndSubPackageExports2, cls1_in_pkg1_1_1, cls1_in_pkg1, false)
  )

  for ((desc, accessDefinitions, clsUsed, clsUsedIn, expectedResult) <- testData) {
    it should desc in {
      val result = new ClassUsageVerifier(AccessDefinitions(accessDefinitions, Map(), Set())).isAllowed(ClassUsage(clsUsed, clsUsedIn,
        MethodBodyUsageDetail("", "", 0)))

      result should be (expectedResult)
    }
  }
}
