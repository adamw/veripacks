package org.veripacks.reader.accessdefinitions

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.veripacks._
import org.objectweb.asm.ClassReader

class AccessDefinitionsReaderTest extends FlatSpec with ShouldMatchers {
  val rootPkg = Pkg("org.veripacks.data.accessdefinitions")

  val readAccessDefinitionsTestData = List(
    (ClassName(rootPkg, "Cls1NoAnnotation"), ExportUndefinedDefinition),
    (ClassName(rootPkg, "Cls2ExportAnnotation"), ExportClassesDefinition(Set(ClassName(rootPkg, "Cls2ExportAnnotation")))),
    (ClassName(rootPkg, "package-info"), ExportAllDefinition)
  )

  for ((className, expectedResult) <- readAccessDefinitionsTestData) {
    it should s"read access definitions in $className" in {
      new AccessDefinitionsReader().readFor(className, new ClassReader(className.fullName)) should be (expectedResult)
    }
  }
}
