package org.veripacks

import ClassUsageFilter._

trait ClassUsageFilter {
  def shouldBeVerified(classUsage: ClassUsage): Result
  def or(second: ClassUsageFilter) = new CompoundClassUsageFilter(this, second)
}

object ClassUsageFilter {
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

object AllUnknownClassUsageFilter extends ClassUsageFilter {
  def shouldBeVerified(classUsage: ClassUsage) = Unknown
}

class CompoundClassUsageFilter(first: ClassUsageFilter, second: ClassUsageFilter) extends ClassUsageFilter {
  def shouldBeVerified(classUsage: ClassUsage) = {
    val firstResult = first.shouldBeVerified(classUsage)
    if (firstResult.isDefinite) firstResult else second.shouldBeVerified(classUsage)
  }
}

abstract class PackagePrefixesClassUsageFilter(packagePrefixes: Iterable[String]) extends ClassUsageFilter {
  protected def classUsageInScope(classUsage: ClassUsage) = {
    val pkg = classUsage.cls.pkg.name
    packagePrefixes.exists(pkg.startsWith)
  }
}

case class ExcludeClassUsageFilter(packagePrefixes: Iterable[String]) extends PackagePrefixesClassUsageFilter(packagePrefixes) {
  def shouldBeVerified(classUsage: ClassUsage) = if (classUsageInScope(classUsage)) {
    No
  } else Unknown
}

case class IncludeClassUsageFilter(packagePrefixes: Iterable[String]) extends PackagePrefixesClassUsageFilter(packagePrefixes) {
  def shouldBeVerified(classUsage: ClassUsage) = if (classUsageInScope(classUsage)) {
    Yes
  } else Unknown
}

