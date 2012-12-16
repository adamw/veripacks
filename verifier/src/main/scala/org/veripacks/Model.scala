package org.veripacks

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

object Pkg {
  def from(pkgName: String) = if (pkgName == RootPkg.name) RootPkg else DefaultPkg(pkgName)
}

case class ClassName(pkg: Pkg, name: String) {
  def fullName = pkg.name + '.' + name
}