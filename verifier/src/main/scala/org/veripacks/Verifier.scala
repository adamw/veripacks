package org.veripacks

class Verifier {
  def verify(rootPackages: Iterable[String]): VerifyResult = {
    VerifyResult(Nil)
  }
}

case class VerifyResult(brokenConstraints: List[ClassUsage])
