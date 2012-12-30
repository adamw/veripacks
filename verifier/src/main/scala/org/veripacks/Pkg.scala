package org.veripacks

sealed trait Pkg {
  def name: String
  def child(childName: String): Pkg
  def isSubpackageOf(other: Pkg): Boolean
}

case object RootPkg extends Pkg {
  val name = ""
  def child(childName: String) = DefaultPkg(childName)
  def isSubpackageOf(other: Pkg) = other == RootPkg
}

case class DefaultPkg(name: String) extends Pkg {
  def child(childName: String) = DefaultPkg(name + '.' + childName)
  def isSubpackageOf(other: Pkg) = other match {
    case RootPkg => true
    case DefaultPkg(otherName) => name.startsWith(otherName)
  }
}

object Pkg {
  def apply(pkgName: String) = if (pkgName == RootPkg.name) RootPkg else DefaultPkg(pkgName)
}
