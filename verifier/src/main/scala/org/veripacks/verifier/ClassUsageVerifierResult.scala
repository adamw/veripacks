package org.veripacks.verifier

import org.veripacks.{Export, Pkg, ClassName}

@Export
sealed trait ClassUsageVerifierResult

object ClassUsageVerifierResult {
  case object Allowed extends ClassUsageVerifierResult {
    override def toString = "allowed"
  }
  case class ClassNotExported(cls: ClassName) extends ClassUsageVerifierResult {
    override def toString = s"class is not exported: ${cls.fullName}"
  }
  case class PackageNotExported(pkg: Pkg) extends ClassUsageVerifierResult {
    override def toString = s"package is not exported: ${pkg.name} by its parent package"
  }
  case class PackageNotImported(pkg: Pkg) extends ClassUsageVerifierResult {
    override def toString = s"package is not imported, but it requires import: ${pkg.name}"
  }
}
