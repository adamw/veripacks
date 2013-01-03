package org.veripacks

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

class VeripacksSelfTest extends FlatSpec with ShouldMatchers {
  it should "not report and contrainst violations in the veripacks code" in {
    new Verifier().verify("org.veripacks").throwIfNotOk
  }
}
