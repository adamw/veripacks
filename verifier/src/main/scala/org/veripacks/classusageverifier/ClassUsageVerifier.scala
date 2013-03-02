package org.veripacks.classusageverifier

import scala.annotation.tailrec
import com.typesafe.scalalogging.slf4j.Logging
import org.veripacks._

class ClassUsageVerifier(accessDefinitions: AccessDefinitions) extends Logging {
  /**
   * Suppose that we are checking if class A can be used in class B.
   * These classes have some first common ancestor, package P. The structure is as follows (<- means is parent of):
   *
   *     Pa1 <- Pa2 <- ... <- Pai (package Pai contains class A)
   * P <-
   *     Pb1 <- Pb2 <- ... <- Pbj (package Paj contains class B)
   *
   * For the usage to be allowed, class A must be exported up to P, as if a class is accessible in P, it is also
   * accessible in all children, hence it is accessible in Pbj as well.
   *
   * Class A is exported up to P if:
   * - Pai exports class A
   * - Pa(i-1) exports package Pai
   * - ...
   * - Pa1 exports package Pa2
   */
  def isAllowed(classUsage: ClassUsage): Boolean = {
    val usedInPkg = classUsage.usedIn.pkg

    val classUsageAllowed = isClassUsageAllowed(classUsage.cls, usedInPkg)
    val pkgUsageAllowed = classUsage.cls.pkg.parent match {
      case None => {
        // Root package, all classes from the root package are accessible
        true
      }
      case Some(parent) => {
        isPkgUsageAllowed(parent, classUsage.cls.pkg, usedInPkg)
      }
    }

    val allowed = classUsageAllowed && pkgUsageAllowed

    logger.debug(s"Usage of ${classUsage.cls.fullName} in ${classUsage.usedIn.fullName} is ${if (allowed) "" else "not "}allowed.")
    allowed
  }

  private def isClassUsageAllowed(cls: ClassName, usedInPkg: Pkg) = {
    if (usedInPkg.isChildPackageOf(cls.pkg)) {
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
  }

  @tailrec
  private def isPkgUsageAllowed(parentPkg: Pkg, childPkg: Pkg, usedInPkg: Pkg): Boolean = {
    if (usedInPkg.isChildPackageOf(parentPkg)) {
      // Allow using packages from parent packages
      true
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
            false
          }
          case Some(grandparentPkg) => isPkgUsageAllowed(grandparentPkg, parentPkg, usedInPkg)
        }
      } else {
        false
      }
    }
  }
}
