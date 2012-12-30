package org.veripacks

case class ClassName(pkg: Pkg, name: String) {
  def fullName = pkg.name + '.' + name
}

object ClassName {
  def fromDottedName(dottedName: String) = {
    val lastDot = dottedName.lastIndexOf('.')
    if (lastDot == -1) {
      ClassName(RootPkg, dottedName)
    } else {
      ClassName(Pkg(dottedName.substring(0, lastDot)), dottedName.substring(lastDot+1))
    }
  }
}