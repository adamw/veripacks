package org.veripacks.verifier

import scala.annotation.tailrec
import com.typesafe.scalalogging.slf4j.Logging
import org.veripacks._
import ClassUsageVerifierResult._

class ClassUsageVerifier(accessDefinitions: AccessDefinitions, requireImportFilter: PkgFilter) extends Logging {
  /**
   * Suppose that we are checking if class A can be used in class B.
   * These classes have some first common ancestor, package P. The structure is as follows (<- means is parent of):
   *
   *     Pa1 <- Pa2 <- ... <- Pai (package Pai contains class A)
   * P <-
   *     Pb1 <- Pb2 <- ... <- Pbj (package Pbj contains class B)
   *
   * For the usage to be allowed:
   * - class A must be exported up to P, as if a class is accessible in P, it is also accessible in all children,
   * hence it is accessible in Pbj as well.
   * - if any of the packages Pb1 ... Pbj require import, the package must be imported by some of the packages
   * (root package) ... Pai.
   *
   * Class A is exported up to P if:
   * - Pai exports class A
   * - Pa(i-1) exports package Pai
   * - ...
   * - Pa1 exports package Pa2
   */
  def verify(classUsage: ClassUsage): ClassUsageVerifierResult = {
    val result = doVerify(List(
      () => isClassUsageAllowedByExport(classUsage),
      () => isPkgUsageAllowedByExport(classUsage),
      () => isPkgUsageAllowedByImport(classUsage)
    ))

    logger.debug(s"Result of verifying usage of ${classUsage.cls.fullName} in ${classUsage.usedIn.fullName} is: ${result}.")

    result
  }

  @tailrec
  private def doVerify(verifiers: List[() => ClassUsageVerifierResult]): ClassUsageVerifierResult = {
    verifiers match {
      case Nil => Allowed
      case v :: other => {
        val result = v()
        if (result != Allowed) result else doVerify(other)
      }
    }
  }

  private def isClassUsageAllowedByExport(classUsage: ClassUsage) = {
    val cls = classUsage.cls

    val result = if (classUsage.usedIn.pkg.isChildPackageOf(cls.pkg)) {
      // Allow using classes from parent packages
      true
    } else {
      val pkgToCheckExportDef = accessDefinitions.exports.getOrElse(cls.pkg, ExportDef.Undefined)

      pkgToCheckExportDef.classes match {
        case ExportSpecificClassesDef(classNames) => {
          classNames.contains(cls)
        }
        case _ => pkgToCheckExportDef.allClassesExported
      }
    }

    if (result) {
      Allowed
    } else {
      ClassNotExported(classUsage.cls)
    }
  }

  private def isPkgUsageAllowedByExport(classUsage: ClassUsage): ClassUsageVerifierResult = {
    classUsage.cls.pkg.parent match {
      case None => {
        // Root package, all classes from the root package are accessible
        Allowed
      }
      case Some(parent) => {
        isPkgUsageAllowedByExport(parent, classUsage.cls.pkg, classUsage.usedIn.pkg)
      }
    }
  }

  @tailrec
  private def isPkgUsageAllowedByExport(parentPkg: Pkg, childPkg: Pkg, usedInPkg: Pkg): ClassUsageVerifierResult = {
    if (usedInPkg.isChildPackageOf(parentPkg)) {
      // Allow using packages from parent packages
      Allowed
    } else {
      val parentExportDef = accessDefinitions.exports.getOrElse(parentPkg, ExportDef.Undefined)
      val parentExportDefExportsChildPkg = parentExportDef.pkgs match {
        case ExportSpecificPkgsDef(pkgs) => {
          pkgs.contains(childPkg)
        }
        case _ => parentExportDef.allPkgsExported
      }

      if (parentExportDefExportsChildPkg) {
        parentPkg.parent match {
          case None => {
            // Shouldn't happen, as only root packages don't have parents, and all packages are subpackages of the root
            // package, so the first "if" would pass.
            PackageNotExported(childPkg)
          }
          case Some(grandparentPkg) => isPkgUsageAllowedByExport(grandparentPkg, parentPkg, usedInPkg)
        }
      } else {
        PackageNotExported(childPkg)
      }
    }
  }

  private def isPkgUsageAllowedByImport(classUsage: ClassUsage): ClassUsageVerifierResult = {
    val toCheck = pkgsWhichMustBeImported(classUsage)
    isPkgUsageAllowedByImport(classUsage.usedIn.pkg, toCheck)
  }

  private def pkgsWhichMustBeImported(classUsage: ClassUsage) = {
    classUsage.cls.pkg
      .allPkgsUpToCommonRoot(classUsage.usedIn.pkg, includeCommonRoot = false)
      .intersect(accessDefinitions.requiresImport)
  }

  @tailrec
  private def isPkgUsageAllowedByImport(pkg: Pkg, toCheck: Set[Pkg]): ClassUsageVerifierResult = {
    if (toCheck.size == 0) {
      Allowed
    } else {
      val importedPkgs = accessDefinitions.importedPkgsFor(pkg)
      val newToCheck = toCheck -- importedPkgs

      pkg.parent match {
        case None => newToCheck.headOption match {
          case None => Allowed
          case Some(newToCheckPkg) => PackageNotImported(newToCheckPkg)
        }
        case Some(parent) => isPkgUsageAllowedByImport(parent, newToCheck)
      }
    }
  }
}
