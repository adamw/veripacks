package org.veripacks

class Verifier {
  def verify(rootPackages: Iterable[String]): VerifyResult = {
    VerifyResult(Nil)
  }
}

case class VerifyResult(brokenConstraints: List[ClassUsage])

case class ClassUsage(cls: ClassName, usedIn: ClassName, line: Int)

sealed trait Pkg {
  def name: String
  def child(childName: String): Pkg
}

case object RootPkg extends Pkg {
  val name = ""
  def child(childName: String) = DefaultPkg(childName)
}

case class DefaultPkg(name: String) extends Pkg {
  def child(childName: String) = DefaultPkg(name + '.' + childName)
}

case class ClassName(pkg: Pkg, name: String)
