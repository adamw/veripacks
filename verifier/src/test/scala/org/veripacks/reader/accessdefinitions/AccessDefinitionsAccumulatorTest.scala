package org.veripacks.reader.accessdefinitions

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.veripacks._
import org.veripacks.ExportClassesDef

class AccessDefinitionsAccumulatorTest extends FlatSpec with ShouldMatchers {
  val pkg1 = Pkg("foo.bar.a")
  val pkg2 = Pkg("foo.bar.b")
  val pkg3 = Pkg("foo.bar.a.aa")

  it should "accumulate several export classes definitions in the same pacakge" in {
    // When
    val acc = new AccessDefinitionsAccumulator
    acc.addExportDefinition(pkg1, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1")))))
    acc.addExportDefinition(pkg1, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls2")))))
    acc.addExportDefinition(pkg1, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls3"), ClassName(pkg1, "cls4")))))

    val result = acc.build

    // Then
    result.isRight should be (true)

    val defs = result.right.get
    defs.exports should have size (1)
    defs.exports(pkg1) should be (ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1"), ClassName(pkg1, "cls2"),
      ClassName(pkg1, "cls3"), ClassName(pkg1, "cls4")))))
  }

  it should "report an error if both export and export all definitions are used for a package" in {
    // When
    val acc = new AccessDefinitionsAccumulator
    acc.addExportDefinition(pkg1, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1")))))
    acc.addExportDefinition(pkg1, ExportDef.All)

    val result = acc.build

    // Then
    result.isLeft should be (true)
  }

  it should "accumulate export all definition" in {
    // When
    val acc = new AccessDefinitionsAccumulator
    acc.addExportDefinition(pkg1, ExportDef.All)
    acc.addExportDefinition(pkg2, ExportDef.All)

    val result = acc.build

    // Then
    result.isRight should be (true)

    val defs = result.right.get
    defs.exports should have size (2)
    defs.exports(pkg1) should be (ExportDef.All)
    defs.exports(pkg2) should be (ExportDef.All)
  }

  it should "accumulate various definitions in one map" in {
    // When
    val acc = new AccessDefinitionsAccumulator
    acc.addExportDefinition(pkg1, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1")))))
    acc.addExportDefinition(pkg1, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls2")))))
    acc.addExportDefinition(pkg2, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg2, "cls3")))))
    acc.addExportDefinition(pkg3, ExportDef.All)

    val result = acc.build

    // Then
    result.isRight should be (true)

    val defs = result.right.get
    defs.exports should have size (3)
    defs.exports(pkg1) should be (ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1"), ClassName(pkg1, "cls2")))))
    defs.exports(pkg2) should be (ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg2, "cls3")))))
    defs.exports(pkg3) should be (ExportDef.All)
  }

  it should "overwrite undefined with specific definitions" in {
    // When
    val acc = new AccessDefinitionsAccumulator
    acc.addExportDefinition(pkg1, ExportDef.Undefined)
    acc.addExportDefinition(pkg1, ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1")))))

    val result = acc.build

    // Then
    result.isRight should be (true)

    val defs = result.right.get
    defs.exports should have size (1)
    defs.exports(pkg1) should be (ExportDef(ExportSpecificClassesDef(Set(ClassName(pkg1, "cls1")))))
  }
}
