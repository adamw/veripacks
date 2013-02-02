package org.veripacks.reader.accessdefinitions

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.veripacks._

class AccessDefinitionsReaderTest extends FlatSpec with ShouldMatchers {
  val rootPkg = Pkg("org.veripacks.data.accessdefinitions")
  val pkgExportSubpkgs = rootPkg.child("pkg_export_subpkgs")
  val pkgExportSubpkgsAllClasses = rootPkg.child("pkg_export_subpkgs_all_classes")

  val readAccessDefinitionsTestData = List(
    (ClassName(rootPkg, "Cls1NoAnnotation"), Set()),
    (ClassName(rootPkg, "Cls2ExportAnnotation"), Set(ExportDef(ExportSpecificClassesDef(Set(ClassName(rootPkg, "Cls2ExportAnnotation")))))),
    (ClassName(rootPkg.child("pkg_export_all"), "package-info"), Set(ExportDef.All)),
    (ClassName(rootPkg.child("pkg_export_all_classes"), "package-info"), Set(ExportDef(ExportAllClassesDef))),
    (ClassName(rootPkg.child("pkg_export_all_subpkgs"), "package-info"), Set(ExportDef(ExportAllPkgsDef))),
    (ClassName(pkgExportSubpkgs, "package-info"), Set(ExportDef(ExportSpecificPkgsDef(
      Set(pkgExportSubpkgs.child("subA"), pkgExportSubpkgs.child("subB"), pkgExportSubpkgs.child("subC")))))),
    (ClassName(pkgExportSubpkgsAllClasses, "package-info"), Set(ExportDef(ExportAllClassesDef), ExportDef(ExportSpecificPkgsDef(
      Set(pkgExportSubpkgsAllClasses.child("sub1"), pkgExportSubpkgsAllClasses.child("sub2"))))))
  )

  for ((className, expectedResult) <- readAccessDefinitionsTestData) {
    it should s"read access definitions in $className" in {
      new AccessDefinitionsReader().readFor(className, ClassReaderProducer.create(className)).toSet should be (expectedResult)
    }
  }
}
