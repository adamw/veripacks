package org.veripacks.reader

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.veripacks.{RootPkg, Pkg, ClassUsage, ClassName}

class ClassUsagesReaderTest extends FlatSpec with ShouldMatchers {
  val rootPkg = Pkg.from("org.veripacks.data.usagesreader")
  val cls1 = ClassName(rootPkg, "Cls1")
  val cls2 = ClassName(rootPkg, "Cls2")

  case class SimpleClassUsage(cls: ClassName, line: Int)
  case class TestData(usagesIn: ClassName, scope: Pkg, expectedResult: Set[SimpleClassUsage])

  val testDatas = List(
    TestData(ClassName(rootPkg, "UsageInVal"), rootPkg, Set(SimpleClassUsage(cls1, 4), SimpleClassUsage(cls2, 5))),
    TestData(ClassName(rootPkg, "UsageInDefReturn"), rootPkg, Set(SimpleClassUsage(cls1, 4), SimpleClassUsage(cls2, 5))),
    TestData(ClassName(rootPkg, "UsageInDefParam"), rootPkg, Set(SimpleClassUsage(cls1, 4), SimpleClassUsage(cls2, 5))),
    TestData(ClassName(rootPkg, "UsageScope"), rootPkg, Set()),
    TestData(ClassName(rootPkg, "UsageScope"), RootPkg, Set(SimpleClassUsage(ClassName(Pkg.from("java.lang"), "String"), 4))),
    TestData(ClassName(rootPkg, "UsageEverywhere"), RootPkg, Set(SimpleClassUsage(cls1, 4), SimpleClassUsage(cls1, 5),
      SimpleClassUsage(cls2, 5)))
  )

  for (testData <- testDatas) {
    it should s"read usages in ${testData.usagesIn.name} with scope ${testData.scope}" in {
      // When
      val result = new ClassUsagesReader().read(testData.usagesIn, testData.scope)

      // Then
      val expectedResult = testData.expectedResult.map(scu => ClassUsage(scu.cls, testData.usagesIn, scu.line))
      result.toSet should be (expectedResult)
    }
  }
}
