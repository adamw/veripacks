package org.veripacks.reader

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.veripacks.{RootPkg, Pkg, ClassName}

class ClassUsagesReaderTest extends FlatSpec with ShouldMatchers {
  val rootPkg = Pkg("org.veripacks.data.usagesreader")
  val cls1 = ClassName(rootPkg, "Cls1")
  val cls2 = ClassName(rootPkg, "Cls2")

  case class SimpleClassUsage(cls: ClassName, line: Int)
  case class TestData(usagesIn: ClassName, scope: Pkg, expectedUsedClasses: Set[ClassName])

  val testDatas = List(
    TestData(ClassName(rootPkg, "UsageInVal"), rootPkg, Set(cls1, cls2)),
    TestData(ClassName(rootPkg, "UsageInField"), rootPkg, Set(cls1, cls2)),
    TestData(ClassName(rootPkg, "UsageInDefReturn"), rootPkg, Set(cls1, cls2)),
    TestData(ClassName(rootPkg, "UsageInDefParam"), rootPkg, Set(cls1, cls2)),
    TestData(ClassName(rootPkg, "UsageScope"), rootPkg, Set()),
    TestData(ClassName(rootPkg, "UsageScope"), RootPkg, Set(ClassName(Pkg("java.lang"), "String"))),
    TestData(ClassName(rootPkg, "UsageEverywhere"), RootPkg, Set(cls1, cls2))
  )

  for (testData <- testDatas) {
    it should s"read usages in ${testData.usagesIn.name} with scope ${testData.scope}" in {
      // When
      val result = new ClassUsagesReader().read(testData.usagesIn, testData.scope)

      // Then
      val usedClasses = result.map(cu => cu.cls).toSet
      usedClasses should be (testData.expectedUsedClasses)
    }
  }
}
