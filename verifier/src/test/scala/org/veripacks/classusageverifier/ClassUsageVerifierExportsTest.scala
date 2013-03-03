package org.veripacks.classusageverifier

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.veripacks._

class ClassUsageVerifierExportsTest extends FlatSpec with ShouldMatchers with ClassUsageVerifierTestData {
  import ClassUsageVerifierResult._

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

  val testData = List[(String, Map[Pkg, ExportDef], ClassName, ClassName, ClassUsageVerifierResult)](
    ("allow using class in same package without access restrictions",
      noExport, cls1_in_pkg1, cls2_in_pkg1, Allowed),
    ("allow using class in same package with access restrictions",
      oneClassExport, cls3_in_pkg1, cls2_in_pkg1, Allowed),
    ("allow using class from parent package despite access restrictions",
      oneClassExport, cls3_in_pkg1, cls1_in_pkg1_1, Allowed),
    ("allow using an exported class",
      oneClassExport, cls1_in_pkg1, cls1_in_pkg2, Allowed),
    ("forbid using a not exported class",
      oneClassExport, cls2_in_pkg1, cls1_in_pkg2, ClassNotExported(cls2_in_pkg1)),
    ("allow using a class when all are exported",
      allExport, cls1_in_pkg1, cls1_in_pkg2, Allowed),
    ("forbid using a not exported class, with multiple access definitions",
      packageAndSubpackageClassExports, cls2_in_pkg1_1, cls2_in_pkg1, ClassNotExported(cls2_in_pkg1_1)),
    ("forbid using a class which is visible only to intermediate packages",
      packageAndSubpackageClassExports, cls1_in_pkg1_1, cls1_in_pkg2, PackageNotExported(pkg1_1)),
    ("allow using a class which is visible only to intermediate packages, in the intermediate package",
      packageAndSubpackageClassExports, cls1_in_pkg1_1, cls2_in_pkg1, Allowed),
    ("forbid using a not exported class from a sub-sub-package, even if the sub-package exports all",
      subSubPackageClassExports, cls2_in_pkg1_1_1, cls1_in_pkg1, ClassNotExported(cls2_in_pkg1_1_1)),
    ("allow using an exported class from a sub-sub-package if the sub-package exports all",
      subSubPackageClassExports, cls1_in_pkg1_1_1, cls1_in_pkg1, Allowed),
    ("allow using a class exported from a sub-sub-package, if the sub-sub-package is exported by the sub-package",
      subSubPackageClassAndSubPackageExports1, cls1_in_pkg1_1_1, cls1_in_pkg1, Allowed),
    ("forbid using a class exported from a sub-sub-package, if the sub-sub-package is not exported by the sub-package",
      subSubPackageClassAndSubPackageExports2, cls1_in_pkg1_1_1, cls1_in_pkg1, PackageNotExported(pkg1_1_1))
  )

  for ((desc, exports, clsUsed, clsUsedIn, expectedResult) <- testData) {
    it should desc in {
      val result = new ClassUsageVerifier(AccessDefinitions(exports, Map(), Set())).verify(ClassUsage(clsUsed, clsUsedIn,
        MethodBodyUsageDetail("", "", 0)))

      result should be (expectedResult)
    }
  }
}
