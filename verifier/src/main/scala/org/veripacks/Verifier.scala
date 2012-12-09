package org.veripacks

import reader.ClassUsagesReader

class Verifier {
  def verify(rootPackages: Iterable[String]): VerifyResult = {
    new ClassUsagesReader().read(rootPackages)
    VerifyResult(Nil)
  }
}

case class VerifyResult(brokenConstraints: List[ClassUsage])

case class ClassUsage(cls: ClassName, usedIn: ClassName, line: Int)

class ClassName(val name: String) extends AnyVal
