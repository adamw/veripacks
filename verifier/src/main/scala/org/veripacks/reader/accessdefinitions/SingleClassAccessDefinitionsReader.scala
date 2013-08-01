package org.veripacks.reader.accessdefinitions

import org.veripacks._
import org.objectweb.asm.{Type, ClassReader}
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.mutable

@Export
class SingleClassAccessDefinitionsReader extends Logging {
  import SingleClassAccessDefinitionsReader._

  def readFor(className: ClassName, classReader: ClassReader): SingleClassAccessDefinitions = {
    val classAnnotationsVisitor = new ClassAnnotationsVisitor()
    classReader.accept(classAnnotationsVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES)

    val annotations = classAnnotationsVisitor.annotationsWithValues
    val exportAnnotations = annotations.filter(awv => ExportAnnotations.contains(awv._1))

    val exportDefs = exportAnnotations.map(awv => exportDefFromAnnotation(className, awv._1, awv._2))
    val importDef = importDefFromAnnotations(annotations)
    val requiresImport = annotations.contains(RequiresImportType)

    val verified = !annotations.contains(NotVerifiedType)

    SingleClassAccessDefinitions(exportDefs, importDef, requiresImport, verified)
  }

  private def exportDefFromAnnotation(className: ClassName, annotation: Type, annotationValues: mutable.Map[String, Any]): ExportDef = {
    annotation match {
      case ExportType => {
        logger.debug(s"Found an @Export annotation on ${className.fullName}.")
        ExportDef(ExportSpecificClassesDef(Set(className)))
      }
      case ExportAllType => {
        logger.debug(s"Found an @ExportAll annotation on ${className.fullName}.")
        ExportDef(ExportAllClassesDef, ExportAllPkgsDef)
      }
      case ExportAllClassesType => {
        logger.debug(s"Found an @ExportAllClasses annotation on ${className.fullName}.")
        ExportDef(ExportAllClassesDef)
      }
      case ExportAllSubpackagesType => {
        logger.debug(s"Found an @ExportAllSubpackages annotation on ${className.fullName}.")
        ExportDef(ExportAllPkgsDef)
      }
      case ExportSubpackagesType => {
        logger.debug(s"Found an @ExportSubpackages annotation on ${className.fullName}.")
        // We know that this annotation has a "value" value, and that it is an array
        val valueValue = valueAnnotationValueAsStrings(annotationValues)
        val subpkgs = valueValue.map(className.pkg.child(_)).toSet
        ExportDef(ExportSpecificPkgsDef(subpkgs))
      }
      case _ => ExportDef.Undefined
    }
  }

  private def importDefFromAnnotations(annotationsWithValues: mutable.Map[Type, mutable.HashMap[String, Any]]) = {
    val importPkgs = annotationsWithValues.get(ImportType) match {
      case Some(importValues) => {
        // We know that this annotation has a "value" value, and that it is an array
        val valueValue = valueAnnotationValueAsStrings(importValues)
        val pkgs = valueValue.map(Pkg(_)).toSet
        pkgs
      }
      case None => Set[Pkg]()
    }

    ImportDef(importPkgs)
  }

  private def valueAnnotationValueAsStrings(annotationValues: mutable.Map[String, Any])= {
    annotationValues("value").asInstanceOf[Iterable[String]]
  }
}

object SingleClassAccessDefinitionsReader {
  val ExportType = Type.getType(classOf[Export])
  val ExportAllType = Type.getType(classOf[ExportAll])
  val ExportAllClassesType = Type.getType(classOf[ExportAllClasses])
  val ExportAllSubpackagesType = Type.getType(classOf[ExportAllSubpackages])
  val ExportSubpackagesType = Type.getType(classOf[ExportSubpackages])
  val ImportType = Type.getType(classOf[Import])
  val RequiresImportType = Type.getType(classOf[RequiresImport])
  val NotVerifiedType = Type.getType(classOf[NotVerified])

  val ExportAnnotations = Set(ExportType, ExportAllType, ExportAllClassesType, ExportAllSubpackagesType,
    ExportSubpackagesType)
}