package org.veripacks

import scala.annotation.tailrec
import com.typesafe.scalalogging.slf4j.Logging

class ClassUsageVerifier(accessDefinitions: AccessDefinitions) extends Logging {
  def isAllowed(classUsage: ClassUsage): Boolean = {
    val referencedPkg = classUsage.cls.pkg
    val usedInPkg = classUsage.usedIn.pkg

    val allowed = isAllowed(referencedPkg, usedInPkg, Some(classUsage.cls))
    logger.debug(s"Usage of ${classUsage.cls.fullName} in ${classUsage.usedIn.fullName} is ${if (allowed) "" else "not "}allowed.")
    allowed
  }

  @tailrec
  private def isAllowed(referencedPkg: Pkg, usedInPkg: Pkg, referencedClassNameOpt: Option[ClassName]): Boolean = {
    // Allow using classes from parent packages
    if (usedInPkg.isSubpackageOf(referencedPkg)) {
      true
    } else {
      val referencedExportDef = accessDefinitions.exports.getOrElse(referencedPkg, ExportDef.Undefined)

      val exportDefAllows = referencedExportDef.classes match {
        case ExportSpecificClassesDef(classNames) => {
          referencedClassNameOpt.map(classNames.contains(_)).getOrElse(false)
        }
        case _ => true
      }

      if (exportDefAllows) {
        referencedPkg.parent match {
          // Shouldn't happen, as only root packages don't have parents, and all packages are subpackages of the root
          // package, so the first "if" would pass.
          case None => false
          case Some(parent) => isAllowed(parent, usedInPkg, None)
        }
      } else {
        false
      }
    }
  }
}
