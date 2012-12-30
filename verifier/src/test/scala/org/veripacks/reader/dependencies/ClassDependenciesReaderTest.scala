package org.veripacks.reader.dependencies

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.veripacks.{RootPkg, Pkg, ClassName}
import org.objectweb.asm.ClassReader

class ClassDependenciesReaderTest extends FlatSpec with ShouldMatchers {
  val rootPkg = Pkg("org.veripacks.data.dependenciesreader")
  val rootPkgList = List(rootPkg)

  val cls1 = ClassName(rootPkg, "Cls1")
  val cls2 = ClassName(rootPkg, "Cls2")
  val cls2Builder = ClassName(rootPkg, "Cls2Builder$")
  val cls3 = ClassName(rootPkg, "Cls3")
  val cls3Builder = ClassName(rootPkg, "Cls3Builder$")

  case class TestData(usagesIn: ClassName, scope: Iterable[Pkg], expectedUsedClasses: Set[ClassName])

  val testDatas = List(
    TestData(ClassName(rootPkg, "UsageInVal"), rootPkgList, Set(cls1, cls2)),
    TestData(ClassName(rootPkg, "UsageInField"), rootPkgList, Set(cls1, cls2)),
    TestData(ClassName(rootPkg, "UsageInDefReturn"), rootPkgList, Set(cls1, cls2)),
    TestData(ClassName(rootPkg, "UsageInDefParam"), rootPkgList, Set(cls1, cls2)),
    TestData(ClassName(rootPkg, "UsageScope"), rootPkgList, Set()),
    TestData(ClassName(rootPkg, "UsageScope"), List(Pkg("java.util")), Set(ClassName(Pkg("java.util"), "List"), ClassName(Pkg("java.util.concurrent"), "Executor"))),
    TestData(ClassName(rootPkg, "UsageScope"), List(Pkg("java.util"), Pkg("java.io")), Set(ClassName(Pkg("java.util"), "List"), ClassName(Pkg("java.util.concurrent"), "Executor"), ClassName(Pkg("java.io"), "InputStream"))),
    TestData(ClassName(rootPkg, "UsageInDefBody"), rootPkgList, Set(cls2, cls2Builder, cls3, cls3Builder)),
    TestData(ClassName(rootPkg, "UsageEverywhere"), rootPkgList, Set(cls1, cls2))
  )

  for (testData <- testDatas) {
    it should s"read dependencies in ${testData.usagesIn.name} with scope ${testData.scope}" in {
      // When
      val result = new ClassDependenciesReader().read(testData.usagesIn, new ClassReader(testData.usagesIn.fullName), testData.scope)

      // Then
      val usedClasses = result.map(cu => cu.cls).toSet
      usedClasses should be (testData.expectedUsedClasses)
    }
  }
}
