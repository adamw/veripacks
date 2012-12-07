package org.veripacks

class Verifier {
  def verify(rootPackages: Iterable[String]): VerifyResult = {
    VerifyResult(Nil)
  }
}

case class VerifyResult(brokenConstraints: List[ClassUsage])

case class ClassUsage(cls: Class[_], usedIn: Class[_], line: Int)
