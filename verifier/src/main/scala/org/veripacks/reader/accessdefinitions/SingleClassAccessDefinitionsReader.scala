package org.veripacks.reader.accessdefinitions

import org.veripacks._
import org.objectweb.asm.{Type, ClassReader}
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.mutable

@Export
class SingleClassAccessDefinitionsReader extends Logging {
  import SingleClassAccessDefinitionsReader._

  def readFor(className: ClassName, classReader: ClassReader): Iterable[ExportDef] = {
    val classAnnotationsVisitor = new ClassAnnotationsVisitor()
    classReader.accept(classAnnotationsVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES)

    val exportAnnotations = classAnnotationsVisitor
      .annotationsWithValues
      .filter(awv => ExportAnnotations.contains(awv._1))

    exportAnnotations.map(awv => resultFromAnnotation(className, awv._1, awv._2))
  }

  private def resultFromAnnotation(className: ClassName, annotation: Type, annotationValues: mutable.Map[String, Any]): ExportDef = {
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
        val valueValue = annotationValues("value").asInstanceOf[Iterable[String]]
        val subpkgs = valueValue.map(className.pkg.child(_)).toSet
        ExportDef(ExportSpecificPkgsDef(subpkgs))
      }
      case _ => ExportDef.Undefined
    }
  }
}

object SingleClassAccessDefinitionsReader {
  val ExportType = Type.getType(classOf[Export])
  val ExportAllType = Type.getType(classOf[ExportAll])
  val ExportAllClassesType = Type.getType(classOf[ExportAllClasses])
  val ExportAllSubpackagesType = Type.getType(classOf[ExportAllSubpackages])
  val ExportSubpackagesType = Type.getType(classOf[ExportSubpackages])

  val ExportAnnotations = Set(ExportType, ExportAllType, ExportAllClassesType, ExportAllSubpackagesType,
    ExportSubpackagesType)
}