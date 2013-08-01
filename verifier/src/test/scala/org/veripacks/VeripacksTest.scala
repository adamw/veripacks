package org.veripacks

import data.t1.p11.Class112
import data.t1.p12.Class121
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.veripacks.data.t3.p32.Class321
import org.veripacks.data.t3.p31.Class311
import org.veripacks.data.t6.p61.Class611
import org.veripacks.data.t6.p62.Class621

class VeripacksTest extends FlatSpec with ShouldMatchers {
  it should "report export errors" in {
    // When
    val result = VeripacksBuilder.build.verify(List("org.veripacks.data.t1"))

    // Then
    result match {
      case VerifyResultBrokenConstraints(brokenConstraints) => {
        brokenConstraints.size should be > (0)
        brokenConstraints.map(_._1.cls).toSet should be (Set(from(classOf[Class112])))
        brokenConstraints.map(_._1.usedIn).toSet should be (Set(from(classOf[Class121])))
        brokenConstraints.map(_._1.detail.sourceFileName).toSet should be (Set("Class121.scala"))
      }
      case _ => fail(s"Expected a broken constraints result, but got $result!")
    }
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
    result match {
      case VerifyResultBrokenConstraints(brokenConstraints) => {
        brokenConstraints.size should be > (0)
        brokenConstraints.map(_._1.cls).toSet should be (Set(from(classOf[Class321])))
        brokenConstraints.map(_._1.usedIn).toSet should be (Set(from(classOf[Class311])))
        brokenConstraints.map(_._1.detail.sourceFileName).toSet should be (Set("Class311.scala"))
      }
      case _ => fail(s"Expected a broken constraints result, but got $result!")
    }
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
    result match {
      case VerifyResultBrokenConstraints(brokenConstraints) => {
        brokenConstraints.size should be > (0)
        brokenConstraints.map(_._1.cls).toSet should be (Set(from(classOf[Class621])))
        brokenConstraints.map(_._1.usedIn).toSet should be (Set(from(classOf[Class611])))
        brokenConstraints.map(_._1.detail.sourceFileName).toSet should be (Set("Class611.scala"))
      }
      case _ => fail(s"Expected a broken constraints result, but got $result!")
    }
  }

  private def from(cls: Class[_]) = new ClassName(Pkg(cls.getPackage.getName), cls.getSimpleName)
}
