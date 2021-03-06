package org.veripacks.verifier

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.veripacks._

class ClassUsageVerifierImportsTest extends FlatSpec with ShouldMatchers with ClassUsageVerifierTestData {
   import ClassUsageVerifierResult._

   val testData = List[(String, Map[Pkg, ImportDef], Set[Pkg], ClassName, ClassName, ClassUsageVerifierResult)](
     ("allow using class from a package which requires import, if the package is imported by the class's package",
       Map(pkg1 -> ImportDef(Set(pkg2))), Set(pkg2), cls1_in_pkg2, cls1_in_pkg1, Allowed),
     ("allow using class from a package which requires import, if the package is imported by a parent package of the used in class",
       Map(pkg1 -> ImportDef(Set(pkg2))), Set(pkg2), cls1_in_pkg2, cls1_in_pkg1_1, Allowed),
     ("allow using class from a package which requires import, if the package is imported by a parent package of both classes",
       Map(pkg1 -> ImportDef(Set(pkg1_1_1))), Set(pkg1_1_1), cls1_in_pkg1_1_1, cls1_in_pkg1_1_2, Allowed),
     ("allow using class from a package which requires import, but the require is on a common parent of the classes",
       Map(), Set(pkg1_1), cls1_in_pkg1_1_1, cls1_in_pkg1_1_2, Allowed),
     ("allow using class from a package which has child packages requiring import",
       Map(), Set(pkg1_1), cls1_in_pkg1, cls1_in_pkg2, Allowed),
     ("forbid using class from a package which requires import, if the package is not imported",
       Map(), Set(pkg2), cls1_in_pkg2, cls1_in_pkg1, PackageNotImported(pkg2)),
     ("forbid using class from a package which parent package requires import, if the package is not imported",
       Map(), Set(pkg1), cls1_in_pkg1_1, cls1_in_pkg2, PackageNotImported(pkg1))
   )

   for ((desc, imports, requiresImport, clsUsed, clsUsedIn, expectedResult) <- testData) {
     it should desc in {
       val result = new ClassUsageVerifier(AccessDefinitions(Map(), imports, requiresImport), AllUnknownPkgFilter)
         .verify(ClassUsage(clsUsed, clsUsedIn, MethodBodyUsageDetail("", "", 0)))

       result should be (expectedResult)
     }
   }

  val smlPkg = Pkg("com.softwaremill")
  val smlCommonPkg = smlPkg.child("common")
  val smlCommonClass = ClassName(smlCommonPkg.child("util"), "RichString")

  val thirdPartyTestData = List[(String, Map[Pkg, ImportDef], ClassName, ClassName, ClassUsageVerifierResult)](
    ("allow using a class from a 3rd party library package if the package is imported and included by the filter",
      Map(pkg1 -> ImportDef(Set(smlPkg))), smlCommonClass, cls1_in_pkg1_1, Allowed),
    ("allow using a class from a 3rd party library package if the package is imported and a parent is included by the filter",
      Map(pkg1 -> ImportDef(Set(smlCommonPkg))), smlCommonClass, cls1_in_pkg1_1, Allowed),
    ("forbid using a class from a 3rd party library package if the package is not imported and included by the filter",
      Map(), smlCommonClass, cls1_in_pkg1_1, PackageNotImported(smlCommonClass.pkg))
  )

  for ((desc, imports, clsUsed, clsUsedIn, expectedResult) <- thirdPartyTestData) {
    it should desc in {
      val result = new ClassUsageVerifier(AccessDefinitions(Map(), imports, Set()), IncludePkgFilter(List(smlPkg.name)))
        .verify(ClassUsage(clsUsed, clsUsedIn, MethodBodyUsageDetail("", "", 0)))

      result should be (expectedResult)
    }
  }
 }
