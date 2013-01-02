package org.veripacks

import scala.annotation.tailrec

class ClassUsageVerifier(accessDefinitions: AccessDefinitions) {
  def isAllowed(classUsage: ClassUsage): Boolean = {
    val referencedPkg = classUsage.cls.pkg
    val usedInPkg = classUsage.usedIn.pkg

    isAllowed(referencedPkg, usedInPkg, Some(classUsage.cls))
  }

  @tailrec
  private def isAllowed(referencedPkg: Pkg, usedInPkg: Pkg, referencedClassNameOpt: Option[ClassName]): Boolean = {
    // Allow using classes from parent packages
    if (usedInPkg.isSubpackageOf(referencedPkg)) {
      true
    } else {
      val referencedExportDef = accessDefinitions.exports.getOrElse(referencedPkg, ExportUndefinedDefinition)

      val exportDefAllows = referencedExportDef match {
        case ExportClassesDefinition(classNames) => {
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
