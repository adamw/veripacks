package org.veripacks.reader.accessdefinitions

import org.veripacks._
import scala.collection.mutable.ListBuffer
import com.typesafe.scalalogging.slf4j.Logging

@Export
class AccessDefinitionsAccumulator extends Logging {
  private val exportDefs = collection.mutable.HashMap[Pkg, ExportDef]()
  private val importDefs = collection.mutable.HashMap[Pkg, ImportDef]()
  private val requiresImport = collection.mutable.HashSet[Pkg]()
  private val errors = ListBuffer[AccessDefinitionError]()

  def addSingleClassAccessDefinitions(pkg: Pkg, accessDefinitions: SingleClassAccessDefinitions) {
    accessDefinitions.exportDefs.foreach(addExportDefinition(pkg, _))
    addImportDefinition(pkg, accessDefinitions.importDef)
    if (accessDefinitions.requiresImport) requiresImport.add(pkg)
  }

  private def addExportDefinition(pkg: Pkg, exportDefinition: ExportDef) {
    def mixedExportWithExportAll() = {
      errors += AccessDefinitionError(s"Package ${pkg.name} is annotated with @ExportAll and also contains classes annotated with @Export!")
      None
    }

    def mixedExportSubpackagesWithExportAll() = {
      errors += AccessDefinitionError(s"Package ${pkg.name} is annotated with @ExportAll and with @ExportSubpackages!")
      None
    }

    def duplicatedExportAllClasses() = {
      errors += AccessDefinitionError(s"Package ${pkg.name} is annotated with @ExportAll and with @ExportAllClasses!")
      None
    }

    def duplicatedExportAllPkgs() = {
      errors += AccessDefinitionError(s"Package ${pkg.name} is annotated with @ExportAll and with @ExportAllSubpackages!")
      None
    }

    def mergeClasses(def1: ExportClassesDef, def2: ExportClassesDef) = {
      (def1, def2) match {
        case (ExportClassesUndefinedDef, other) => Some(other)
        case (other, ExportClassesUndefinedDef) => Some(other)
        case (ExportAllClassesDef, ExportAllClassesDef) => duplicatedExportAllClasses()
        case (ExportAllClassesDef, _) => mixedExportWithExportAll()
        case (_, ExportAllClassesDef) => mixedExportWithExportAll()
        case (ExportSpecificClassesDef(set1), ExportSpecificClassesDef(set2)) => {
          Some(ExportSpecificClassesDef(set1 ++ set2))
        }
      }
    }

    def mergePkgs(def1: ExportPkgsDef, def2: ExportPkgsDef) = {
      (def1, def2) match {
        case (ExportPkgsUndefinedDef, other) => Some(other)
        case (other, ExportPkgsUndefinedDef) => Some(other)
        case (ExportAllPkgsDef, ExportAllPkgsDef) => duplicatedExportAllPkgs()
        case (ExportAllPkgsDef, _) => mixedExportSubpackagesWithExportAll()
        case (_, ExportAllPkgsDef) => mixedExportSubpackagesWithExportAll()
        case (ExportSpecificPkgsDef(set1), ExportSpecificPkgsDef(set2)) => {
          Some(ExportSpecificPkgsDef(set1 ++ set2))
        }
      }
    }

    def merge(def1: ExportDef, def2: ExportDef) {
      for {
        mergedClasses <- mergeClasses(def1.classes, def2.classes)
        mergedPkgs <- mergePkgs(def1.pkgs, def2.pkgs)
      } {
        exportDefs(pkg) = ExportDef(mergedClasses, mergedPkgs)
      }
    }

    val current = exportDefs.get(pkg)
    current match {
      case None => exportDefs(pkg) = exportDefinition
      case Some(existing) => {
        merge(existing, exportDefinition)
      }
    }
  }

  private def addImportDefinition(pkg: Pkg, importDef: ImportDef) {
    def merge(importDef1: ImportDef, importDef2: ImportDef) = {
      ImportDef(importDef1.pkgs ++ importDef2.pkgs)
    }

    if (importDef.pkgs.size > 0) {
      importDefs.put(pkg, merge(importDefs.getOrElse(pkg, ImportDef(Set())), importDef))
    }
  }

  def build: Either[List[AccessDefinitionError], AccessDefinitions] = {
    validate()

    if (errors.size == 0) {
      logger.debug(s"Building access definitions; Exports: ${exportDefs.size}, imports: ${importDefs.size}, " +
        s"requires import: ${requiresImport.size}")
      Right(AccessDefinitions(exportDefs.toMap, importDefs.toMap, requiresImport.toSet))
    } else {
      logger.debug(s"Building access definitions; Number of errors: ${errors.size}")
      Left(errors.toList)
    }
  }

  private def validate() {
    validateImports()
  }

  private def validateImports() {
    for {
      (pkg, importDef) <- importDefs
      importedPkg <- importDef.pkgs
    } {
      def validate_importingOnlyPackagesWhichRequireImport() {
        if (!requiresImport.contains(importedPkg)) {
          errors += AccessDefinitionError(s"Package ${pkg.name} imports package ${importedPkg.name}, but it doesn't require importing")
        }
      }

      def validate_importingNonParentPackages() {
        if (pkg.isChildPackageOf(importedPkg)) {
          errors += AccessDefinitionError(s"Package ${pkg.name} imports package ${importedPkg.name}, but it is a parent package")
        }
      }

      validate_importingOnlyPackagesWhichRequireImport()
      validate_importingNonParentPackages()
    }
  }


}
