package org.veripacks

import data.t1.p11.Class112
import data.t1.p12.Class121
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

class VerifierTest extends FlatSpec with ShouldMatchers {
  it should "report errors" in {
    // When
    val result = new Verifier().verify(List("org.veripacks.data.t1"))

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

  it should "report no errors" in {
    // When
    val result = new Verifier().verify(List("org.veripacks.data.t2"))

    // Then
    result should be (VerifyResultOk)
  }

  private def from(cls: Class[_]) = new ClassName(Pkg(cls.getPackage.getName), cls.getSimpleName)
}
