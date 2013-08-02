package org.veripacks

import PkgFilter._

trait PkgFilter {
  def includes(pkg: Pkg): Result
  def or(second: PkgFilter) = new CompoundPkgFilter(this, second)
}

object PkgFilter {
  sealed trait Result {
    def isDefinite: Boolean
  }
  case object Yes extends Result {
    def isDefinite = true
  }
  case object No extends Result {
    def isDefinite = true
  }
  case object Unknown extends Result {
    def isDefinite = false
  }
}

object AllUnknownPkgFilter extends PkgFilter {
  def includes(pkg: Pkg) = Unknown
}

class CompoundPkgFilter(first: PkgFilter, second: PkgFilter) extends PkgFilter {
  def includes(pkg: Pkg) = {
    val firstResult = first.includes(pkg)
    if (firstResult.isDefinite) firstResult else second.includes(pkg)
  }
}

abstract class PackagePrefixesPkgFilter(packagePrefixes: Iterable[String]) extends PkgFilter {
  protected def pkgInScope(pkg: Pkg): Boolean = {
    packagePrefixes.find(pkg.name.startsWith) match {
      case Some(prefix) => {
        // Making sure only whole package name components match
        prefix == "" || prefix == pkg.name || pkg.name.startsWith(prefix + ".")
      }
      case None => false
    }
  }
}

case class ExcludePkgFilter(packagePrefixes: Iterable[String]) extends PackagePrefixesPkgFilter(packagePrefixes) {
  def includes(pkg: Pkg) = if (pkgInScope(pkg)) {
    No
  } else Unknown
}

case class IncludePkgFilter(packagePrefixes: Iterable[String]) extends PackagePrefixesPkgFilter(packagePrefixes) {
  def includes(pkg: Pkg) = if (pkgInScope(pkg)) {
    Yes
  } else Unknown
}

