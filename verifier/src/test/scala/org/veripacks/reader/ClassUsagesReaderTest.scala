package org.veripacks.reader

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.veripacks.{Pkg, ClassUsage, ClassName}

class ClassUsagesReaderTest extends FlatSpec with ShouldMatchers {
  val rootPkg = Pkg.from("org.veripacks.data.usagesreader")
  val cls1 = ClassName(rootPkg, "Cls1")
  val cls2 = ClassName(rootPkg, "Cls2")

  case class SimpleClassUsage(cls: ClassName, line: Int)
  case class TestData(usagesIn: ClassName, scope: Pkg, expectedResult: Set[SimpleClassUsage])

  val testDatas = List(
    TestData(ClassName(rootPkg, "UsageInVal"), rootPkg, Set(SimpleClassUsage(cls1, 4), SimpleClassUsage(cls2, 5)))
  )

  for (testData <- testDatas) {
    it should s"read usages in ${testData.usagesIn.name}" in {
      // When
      val result = new ClassUsagesReader().read(testData.usagesIn, testData.scope)

      // Then
      val expectedResult = testData.expectedResult.map(scu => ClassUsage(scu.cls, testData.usagesIn, scu.line))
      result.toSet should be (expectedResult)
    }
  }
}
