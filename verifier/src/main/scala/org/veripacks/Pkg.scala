package org.veripacks

sealed trait Pkg {
  def name: String
  def child(childName: String): Pkg
  def isChildPackageOf(other: Pkg): Boolean
  def parent: Option[Pkg]
  def allPkgsUpToCommonRoot(other: Pkg): Set[Pkg]
}

case object RootPkg extends Pkg {
  val name = ""
  def child(childName: String) = DefaultPkg(childName)
  def isChildPackageOf(other: Pkg) = other == RootPkg
  def parent = None
  def allPkgsUpToCommonRoot(other: Pkg) = Set(RootPkg)
}

case class DefaultPkg(name: String) extends Pkg {
  def child(childName: String) = DefaultPkg(name + '.' + childName)

  def isChildPackageOf(other: Pkg) = other match {
    case RootPkg => true
    case DefaultPkg(otherName) => name == otherName || name.startsWith(otherName + '.')
  }

  private def theParent: Pkg = {
    val lastDot = name.lastIndexOf('.')
    if (lastDot == -1) {
      RootPkg
    } else {
      DefaultPkg(name.substring(0, lastDot))
    }
  }

  def parent = Some(theParent)

  def allPkgsUpToCommonRoot(other: Pkg) = {
    if (other.isChildPackageOf(this)) {
      Set(this)
    } else {
      theParent.allPkgsUpToCommonRoot(other) + this
    }
  }
}

object Pkg {
  def apply(pkgName: String): Pkg = if (pkgName == RootPkg.name) RootPkg else DefaultPkg(pkgName)
}
