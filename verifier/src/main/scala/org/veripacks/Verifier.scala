package org.veripacks

class Verifier {
  def verify(rootPackages: Iterable[String]): VerifyResult = {
    VerifyResult(Nil)
  }
}

case class VerifyResult(brokenConstraints: List[ClassUsage])

case class ClassUsage(cls: ClassName, usedIn: ClassName, line: Int)

case class ClassName(name: String)
