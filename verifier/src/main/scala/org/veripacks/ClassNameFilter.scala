package org.veripacks

import ClassNameFilter._

trait ClassNameFilter {
  def includes(className: ClassName): Result
  def or(second: ClassNameFilter) = new CompoundClassNameFilter(this, second)
}

object ClassNameFilter {
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

object AllUnknownClassNameFilter extends ClassNameFilter {
  def includes(className: ClassName) = Unknown
}

class CompoundClassNameFilter(first: ClassNameFilter, second: ClassNameFilter) extends ClassNameFilter {
  def includes(className: ClassName) = {
    val firstResult = first.includes(className)
    if (firstResult.isDefinite) firstResult else second.includes(className)
  }
}

abstract class PackagePrefixesClassNameFilter(packagePrefixes: Iterable[String]) extends ClassNameFilter {
  protected def classNameInScope(className: ClassName) = {
    val pkg = className.pkg.name
    packagePrefixes.exists(pkg.startsWith)
  }
}

case class ExcludeClassNameFilter(packagePrefixes: Iterable[String]) extends PackagePrefixesClassNameFilter(packagePrefixes) {
  def includes(className: ClassName) = if (classNameInScope(className)) {
    No
  } else Unknown
}

case class IncludeClassNameFilter(packagePrefixes: Iterable[String]) extends PackagePrefixesClassNameFilter(packagePrefixes) {
  def includes(className: ClassName) = if (classNameInScope(className)) {
    Yes
  } else Unknown
}

