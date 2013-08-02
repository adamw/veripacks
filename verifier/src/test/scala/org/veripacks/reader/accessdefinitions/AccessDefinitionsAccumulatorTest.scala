package org.veripacks.reader.accessdefinitions

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.veripacks._
import com.typesafe.scalalogging.slf4j.Logging

class AccessDefinitionsAccumulatorTest extends FlatSpec with ShouldMatchers with Logging {
  val pkg1 = Pkg("foo.bar.a")
  val pkg1sub1 = Pkg("foo.bar.a.sub1")
  val pkg1sub2 = Pkg("foo.bar.a.sub2")
  val pkg2 = Pkg("foo.bar.b")
  val pkg3 = Pkg("foo.bar.a.aa")
  val pkg3sub1 = Pkg("foo.bar.a.aa.sub1")

  it should "accumulate several export classes definitions in the same pacakge" in {
    // When
    val acc = new AccessDefinitionsAccumulator(AllUnknownPkgFilter)
    addExportDefinition(acc, pkg1, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1")))))
    addExportDefinition(acc, pkg1, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls2")))))
    addExportDefinition(acc, pkg1, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls3"), ClassName(pkg1, "cls4")))))

    val result = acc.build

    // Then
    result should be ('right)

    val defs = result.right.get
    defs.exports should have size (1)
    defs.exports(pkg1) should be (ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1"), ClassName(pkg1, "cls2"),
      ClassName(pkg1, "cls3"), ClassName(pkg1, "cls4")))))
  }

  it should "report an error if both export and export all definitions are used for a package" in {
    // When
    val acc = new AccessDefinitionsAccumulator(AllUnknownPkgFilter)
    addExportDefinition(acc, pkg1, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1")))))
    addExportDefinition(acc, pkg1, ExportDef.All)

    val result = acc.build

    // Then
    result should be ('left)

    logger.info(s"Verification errors: ${result.left.get}")
  }

  it should "accumulate export all definition" in {
    // When
    val acc = new AccessDefinitionsAccumulator(AllUnknownPkgFilter)
    addExportDefinition(acc, pkg1, ExportDef.All)
    addExportDefinition(acc, pkg2, ExportDef.All)

    val result = acc.build

    // Then
    result should be ('right)

    val defs = result.right.get
    defs.exports should have size (2)
    defs.exports(pkg1) should be (ExportDef.All)
    defs.exports(pkg2) should be (ExportDef.All)
  }

  it should "accumulate various definitions in one map" in {
    // When
    val acc = new AccessDefinitionsAccumulator(AllUnknownPkgFilter)
    addExportDefinition(acc, pkg1, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1")))))
    addExportDefinition(acc, pkg1, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls2")))))
    addExportDefinition(acc, pkg2, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg2, "cls3"))), ExportAllPkgsDef))
    addExportDefinition(acc, pkg3, ExportDef.All)

    val result = acc.build

    // Then
    result should be ('right)

    val defs = result.right.get
    defs.exports should have size (3)
    defs.exports(pkg1) should be (ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1"), ClassName(pkg1, "cls2")))))
    defs.exports(pkg2) should be (ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg2, "cls3"))), ExportAllPkgsDef))
    defs.exports(pkg3) should be (ExportDef.All)
  }

  it should "overwrite undefined with specific definitions" in {
    // When
    val acc = new AccessDefinitionsAccumulator(AllUnknownPkgFilter)
    addExportDefinition(acc, pkg1, ExportDef.Undefined)
    addExportDefinition(acc, pkg1, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1")))))

    val result = acc.build

    // Then
    result should be ('right)

    val defs = result.right.get
    defs.exports should have size (1)
    defs.exports(pkg1) should be (ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1")))))
  }

  it should "not overwrite specific with undefined definitions" in {
    // When
    val acc = new AccessDefinitionsAccumulator(AllUnknownPkgFilter)
    addExportDefinition(acc, pkg1, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1")))))
    addExportDefinition(acc, pkg1, ExportDef.Undefined)

    val result = acc.build

    // Then
    result should be ('right)

    val defs = result.right.get
    defs.exports should have size (1)
    defs.exports(pkg1) should be (ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1")))))
  }

  it should "accumulate both export classes and export subpackages" in {
    // When
    val acc = new AccessDefinitionsAccumulator(AllUnknownPkgFilter)
    addExportDefinition(acc, pkg1, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1")))))
    addExportDefinition(acc, pkg1, ExportDef(ExportSpecificPkgsDef(Set(pkg1sub1))))

    addExportDefinition(acc, pkg2, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg2, "cls2")))))
    addExportDefinition(acc, pkg2, ExportDef(ExportAllPkgsDef))

    addExportDefinition(acc, pkg3, ExportDef(ExportAllClassesDef))
    addExportDefinition(acc, pkg3, ExportDef(ExportSpecificPkgsDef(Set(pkg3sub1))))

    val result = acc.build

    // Then
    result should be ('right)

    val defs = result.right.get
    defs.exports should have size (3)

    defs.exports(pkg1) should be (ExportDef(
      ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1"))),
      ExportSpecificPkgsDef(Set(pkg1sub1))))

    defs.exports(pkg2) should be (ExportDef(
      ExportSpecificClassesDef(Set(ClassName(pkg2, "cls2"))),
      ExportAllPkgsDef))

    defs.exports(pkg3) should be (ExportDef(
      ExportAllClassesDef,
      ExportSpecificPkgsDef(Set(pkg3sub1))))
  }

  it should "report an error if both export all classes/subpackages and export all definitions are used for a package" in {
    // When
    val acc = new AccessDefinitionsAccumulator(AllUnknownPkgFilter)
    addExportDefinition(acc, pkg1, ExportDef(ExportAllClassesDef))
    addExportDefinition(acc, pkg1, ExportDef.All)

    addExportDefinition(acc, pkg2, ExportDef(ExportAllPkgsDef))
    addExportDefinition(acc, pkg2, ExportDef.All)

    val result = acc.build

    // Then
    result should be ('left)

    logger.info(s"Verification errors: ${result.left.get}")
    result.left.get should have size (2)
  }

  it should "accumulate import definitions" in {
    // When
    val acc = new AccessDefinitionsAccumulator(AllUnknownPkgFilter)
    acc.addSingleClassAccessDefinitions(pkg1, ClassAccessDefinitions(Nil, ImportDef(Set(pkg2, pkg3)), requiresImport = false, verified = true))
    acc.addSingleClassAccessDefinitions(pkg2, ClassAccessDefinitions(Nil, ImportDef(Set(pkg3sub1)), requiresImport = true, verified = true))
    acc.addSingleClassAccessDefinitions(pkg3, ClassAccessDefinitions(Nil, ImportDef(Set()), requiresImport = true, verified = true))
    acc.addSingleClassAccessDefinitions(pkg3sub1, ClassAccessDefinitions(Nil, ImportDef(Set()), requiresImport = true, verified = true))

    val result = acc.build

    // Then
    result should be ('right)
    val defs = result.right.get

    defs.imports(pkg1) should be (ImportDef(Set(pkg2, pkg3)))
    defs.imports(pkg2) should be (ImportDef(Set(pkg3sub1)))

    defs.requiresImport should be (Set(pkg2, pkg3, pkg3sub1))
  }

  it should "accumulate both import and export definitions" in {
    // When
    val acc = new AccessDefinitionsAccumulator(AllUnknownPkgFilter)
    acc.addSingleClassAccessDefinitions(pkg1, ClassAccessDefinitions(List(ExportDef(ExportAllClassesDef)), ImportDef(Set(pkg2)), requiresImport = true, verified = true))
    acc.addSingleClassAccessDefinitions(pkg2, ClassAccessDefinitions(Nil, ImportDef(Set(pkg1)), requiresImport = false, verified = true))
    acc.addSingleClassAccessDefinitions(pkg2, ClassAccessDefinitions(List(ExportDef(ExportAllPkgsDef)), ImportDef(Set(pkg3)), requiresImport = true, verified = true))
    acc.addSingleClassAccessDefinitions(pkg3, ClassAccessDefinitions(Nil, ImportDef(Set()), requiresImport = true, verified = true))

    val result = acc.build

    // Then
    result should be ('right)
    val defs = result.right.get

    defs.exports(pkg1) should be (ExportDef(ExportAllClassesDef))
    defs.exports(pkg2) should be (ExportDef(ExportAllPkgsDef))

    defs.imports(pkg1) should be (ImportDef(Set(pkg2)))
    defs.imports(pkg2) should be (ImportDef(Set(pkg3, pkg1)))

    defs.requiresImport should be (Set(pkg1, pkg2, pkg3))
  }

  it should "report an error when importing a package which doesn't require import" in {
    // When
    val acc = new AccessDefinitionsAccumulator(AllUnknownPkgFilter)
    acc.addSingleClassAccessDefinitions(pkg1, ClassAccessDefinitions(Nil, ImportDef(Set(pkg2)), requiresImport = false, verified = true))

    val result = acc.build

    // Then
    result should be ('left)

    logger.info(s"Verification errors: ${result.left.get}")
    result.left.get should have size (1)
  }

  it should "report an error when importing a parent package" in {
    // When
    val acc = new AccessDefinitionsAccumulator(AllUnknownPkgFilter)
    acc.addSingleClassAccessDefinitions(pkg1, ClassAccessDefinitions(Nil, ImportDef(Set()), requiresImport = true, verified = true))
    acc.addSingleClassAccessDefinitions(pkg1sub1, ClassAccessDefinitions(Nil, ImportDef(Set(pkg1)), requiresImport = false, verified = true))

    val result = acc.build

    // Then
    result should be ('left)

    logger.info(s"Verification errors: ${result.left.get}")
    result.left.get should have size (1)
  }

  private def addExportDefinition(acc: AccessDefinitionsAccumulator, pkg: Pkg, exportDef: ExportDef) {
    acc.addSingleClassAccessDefinitions(pkg, ClassAccessDefinitions(List(exportDef), ImportDef(Set()), requiresImport = false, verified = true))
  }
}
