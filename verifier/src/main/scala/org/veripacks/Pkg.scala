package org.veripacks

sealed trait Pkg {
  def name: String
  def child(childName: String): Pkg
  def isSubpackageOf(other: Pkg): Boolean
  def parent: Option[Pkg]
}

case object RootPkg extends Pkg {
  val name = ""
  def child(childName: String) = DefaultPkg(childName)
  def isSubpackageOf(other: Pkg) = other == RootPkg
  def parent = None
}

case class DefaultPkg(name: String) extends Pkg {
  def child(childName: String) = DefaultPkg(name + '.' + childName)
  def isSubpackageOf(other: Pkg) = other match {
    case RootPkg => true
    case DefaultPkg(otherName) => name.startsWith(otherName)
  }
  def parent = {
    val lastDot = name.lastIndexOf('.')
    if (lastDot == -1) {
      Some(RootPkg)
    } else {
      Some(DefaultPkg(name.substring(0, lastDot)))
    }
  }
}

object Pkg {
  def apply(pkgName: String): Pkg = if (pkgName == RootPkg.name) RootPkg else DefaultPkg(pkgName)
}
