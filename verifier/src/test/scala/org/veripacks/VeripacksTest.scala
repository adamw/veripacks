package org.veripacks

import data.t1.p11.Class112
import data.t1.p12.Class121
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.veripacks.data.t3.p32.Class321
import org.veripacks.data.t3.p31.Class311
import org.veripacks.data.t6.p61.Class611
import org.veripacks.data.t6.p62.Class621
import org.veripacks.data.t7.Class71

class VeripacksTest extends FlatSpec with ShouldMatchers {
  it should "report export errors" in {
    // When
    val result = VeripacksBuilder.build.verify(List("org.veripacks.data.t1"))

    // Then
    verifyBrokenConstraintsOnlyFrom(result, classOf[Class112], classOf[Class121])
  }

  it should "report no export errors" in {
    // When
    val result = VeripacksBuilder.build.verify(List("org.veripacks.data.t2"))

    // Then
    result should be (VerifyResultOk)
  }

  it should "report import errors" in {
    // When
    val result = VeripacksBuilder.build.verify(List("org.veripacks.data.t3"))

    // Then
    verifyBrokenConstraintsOnlyFrom(result, classOf[Class321], classOf[Class311])
  }

  it should "report no import errors" in {
    // When
    val result = VeripacksBuilder.build.verify(List("org.veripacks.data.t4"))

    // Then
    result should be (VerifyResultOk)
  }

  it should "report no errors when verification is skipped" in {
    // When
    val result = VeripacksBuilder.build.verify(List("org.veripacks.data.t5"))

    // Then
    result should be (VerifyResultOk)
  }

  it should "report import errors basing on custom-provided metadata" in {
    // When
    val result = VeripacksBuilder
      .withCustomAccessDefinitionReader(new CustomAccessDefinitionsReader {
        override def isRequiresImport(pkg: Pkg) = pkg.name == "org.veripacks.data.t6.p62"
      })
      .build
      .verify(List("org.veripacks.data.t6"))

    // Then
    verifyBrokenConstraintsOnlyFrom(result, classOf[Class621], classOf[Class611])
  }

  it should "report import errors when 3rd party libs are checked and not imported" in {
    // When
    val result = VeripacksBuilder
      .requireImportOf("com.typesafe")
      .build
      .verify(List("org.veripacks.data.t7"))

    // Then
    verifyBrokenConstraintsOnlyFrom(result, classOf[com.typesafe.scalalogging.slf4j.Logger], classOf[Class71],
      clsSuffix = "$")
  }

  it should "report no import errors when 3rd party libs are checked and imported" in {
    // When
    val result = VeripacksBuilder
      .requireImportOf("com.typesafe")
      .build
      .verify(List("org.veripacks.data.t8"))

    // Then
    result should be (VerifyResultOk)
  }

  def verifyBrokenConstraintsOnlyFrom(result: VerifyResult, cls: Class[_], usedIn: Class[_], clsSuffix: String = "") = {
    result match {
      case VerifyResultBrokenConstraints(brokenConstraints) => {
        brokenConstraints.size should be > (0)
        brokenConstraints.map(_._1.cls).toSet should be (Set(from(cls, clsSuffix)))
        brokenConstraints.map(_._1.usedIn).toSet should be (Set(from(usedIn)))
        brokenConstraints.map(_._1.detail.sourceFileName).toSet should be (Set(s"${usedIn.getSimpleName}.scala"))
      }
      case _ => fail(s"Expected a broken constraints result, but got $result!")
    }
  }

  private def from(cls: Class[_], suffix: String = "") = new ClassName(Pkg(cls.getPackage.getName), cls.getSimpleName + suffix)
}
