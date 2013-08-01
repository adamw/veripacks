package org.veripacks.reader.accessdefinitions

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.veripacks._
import org.veripacks.reader.ClassReaderProducer

class ClassAccessDefinitionsReaderTest extends FlatSpec with ShouldMatchers {
  val rootPkg = Pkg("org.veripacks.data.accessdefinitions")
  val pkgExportSubpkgs = rootPkg.child("pkg_export_subpkgs")
  val pkgExportSubpkgsAllClasses = rootPkg.child("pkg_export_subpkgs_all_classes")
  val pkgMixed = rootPkg.child("pkg_mixed")

  val readExportAccessDefinitionsTestData = List(
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

  for ((className, expectedExportDefsResult) <- readExportAccessDefinitionsTestData) {
    it should s"read export access definitions in $className" in {
      val result = new ClassAccessDefinitionsReader().readFor(className, ClassReaderProducer.create(className))

      result.exportDefs.toSet should be (expectedExportDefsResult)
      result.importDef.pkgs should be (Set())
      result.requiresImport should be (false)
      result.verified should be (true)
    }
  }

  val readImportAccessDefinitionsTestData = List(
    (ClassName(rootPkg.child("pkg_import"), "package-info"),
      ClassAccessDefinitions(Nil, ImportDef(Set(Pkg("foo.bar.pkg1"), Pkg("foo.bar.pkg2"))), requiresImport = false, verified = true)),
    (ClassName(rootPkg.child("pkg_requires_import"), "package-info"),
      ClassAccessDefinitions(Nil, ImportDef(Set()), requiresImport = true, verified = true)),
    (ClassName(pkgMixed, "package-info"), ClassAccessDefinitions(Set(ExportDef(ExportSpecificPkgsDef(Set(pkgMixed.child("sub1"))))),
      ImportDef(Set(Pkg("foo.bar"))), requiresImport = true, verified = true))
  )

  for ((className, expectedResult) <- readImportAccessDefinitionsTestData) {
    it should s"read import access definitions in $className" in {
      val result = new ClassAccessDefinitionsReader().readFor(className, ClassReaderProducer.create(className))

      result.exportDefs.toSet should be (expectedResult.exportDefs.toSet)
      result.importDef should be (expectedResult.importDef)
      result.requiresImport should be (expectedResult.requiresImport)
      result.verified should be (expectedResult.verified)
    }
  }

  it should s"read not verified annotation" in {
    // Given
    val className = ClassName(rootPkg, "Cls3NotVerifiedAnnotation")

    // When
    val result = new ClassAccessDefinitionsReader().readFor(className, ClassReaderProducer.create(className))

    // Then
    result.verified should be (false)
  }
}
