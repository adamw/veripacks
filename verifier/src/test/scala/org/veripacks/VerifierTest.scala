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
    result.brokenConstraints should be (List(ClassUsage(from(classOf[Class112]), from(classOf[Class121]),
      MethodSignatureUsageDetail("Class121.scala", "i2"))))
  }

  it should "report no errors" in {
    // When
    val result = new Verifier().verify(List("org.veripacks.data.t2"))

    // Then
    result.brokenConstraints should be (Nil)
  }

  private def from(cls: Class[_]) = new ClassName(Pkg(cls.getPackage.toString), cls.getName)
}
