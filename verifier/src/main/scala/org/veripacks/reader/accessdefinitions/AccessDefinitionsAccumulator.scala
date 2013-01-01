package org.veripacks.reader.accessdefinitions

import org.veripacks._
import scala.collection.mutable.ListBuffer

@Export
class AccessDefinitionsAccumulator {
  private val defs = collection.mutable.HashMap[Pkg, ExportDefinition]()
  private val errors = ListBuffer[AccessDefinitionError]()

  def addExportDefinition(pkg: Pkg, exportDefinition: ExportDefinition) {
    def mixedExportWithExportAll() {
      errors += AccessDefinitionError(s"Package $pkg is annotated with @ExportAll and also contains classes annotated with @Export!")
    }

    def merge(def1: ExportDefinition, def2: ExportDefinition) {
      (def1, def2) match {
        case (ExportUndefinedDefinition, other) => defs(pkg) = other
        case (other, ExportUndefinedDefinition) => defs(pkg) = other
        case (ExportAllDefinition, ExportAllDefinition) => // Ignore
        case (ExportAllDefinition, _) => mixedExportWithExportAll()
        case (_, ExportAllDefinition) => mixedExportWithExportAll()
        case (ExportClassesDefinition(set1), ExportClassesDefinition(set2)) => {
          defs(pkg) = ExportClassesDefinition(set1 ++ set2)
        }
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
      Right(AccessDefinitions(defs.toMap))
    } else {
      Left(errors.toList)
    }
  }
}
