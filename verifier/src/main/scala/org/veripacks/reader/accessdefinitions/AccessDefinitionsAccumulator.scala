package org.veripacks.reader.accessdefinitions

import org.veripacks._
import scala.collection.mutable.ListBuffer
import com.typesafe.scalalogging.slf4j.Logging

@Export
class AccessDefinitionsAccumulator extends Logging {
  private val defs = collection.mutable.HashMap[Pkg, ExportDef]()
  private val errors = ListBuffer[AccessDefinitionError]()

  def addExportDefinition(pkg: Pkg, exportDefinition: ExportDef) {
    def mixedExportWithExportAll() = {
      errors += AccessDefinitionError(s"Package $pkg is annotated with @ExportAll and also contains classes annotated with @Export!")
      None
    }

    def mixedExportSubpackagesWithExportAll() = {
      errors += AccessDefinitionError(s"Package $pkg is annotated with @ExportAll and with @ExportSubpackages!")
      None
    }

    def duplicatedExportAllClasses() = {
      errors += AccessDefinitionError(s"Package $pkg is annotated with @ExportAll and with @ExportAllClasses!")
      None
    }

    def duplicatedExportAllPkgs() = {
      errors += AccessDefinitionError(s"Package $pkg is annotated with @ExportAll and with @ExportAllSubpackages!")
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
        defs(pkg) = ExportDef(mergedClasses, mergedPkgs)
      }
    }

    val current = defs.get(pkg)
    current match {
      case None => defs(pkg) = exportDefinition
      case Some(existing) => {
        merge(existing, exportDefinition)
      }
    }
  }

  def build: Either[List[AccessDefinitionError], AccessDefinitions] = {
    if (errors.size == 0) {
      logger.debug(s"Building access definitions; Size: ${defs.size}")
      Right(AccessDefinitions(defs.toMap))
    } else {
      logger.debug(s"Building access definitions; Number of errors: ${errors.size}")
      Left(errors.toList)
    }
  }
}
