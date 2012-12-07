package org.veripacks

import data.t1.p11.Class112
import data.t1.p12.Class121
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

class VerifierTest extends FlatSpec with ShouldMatchers {
  it should "work" in {
    // When
    val result = new Verifier().verify(List("org.veripacks.data.t1"))

    // Then
    result.brokenConstraints should be (List(ClassUsage(classOf[Class112], classOf[Class121], 7)))
  }
}
