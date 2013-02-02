package org.veripacks.reader.accessdefinitions

import org.veripacks._
import org.objectweb.asm.{Type, ClassReader}
import com.typesafe.scalalogging.slf4j.Logging

@Export
class AccessDefinitionsReader extends Logging {
  import AccessDefinitionsReader._

  def readFor(className: ClassName, classReader: ClassReader): ExportDef = {
    val classAnnotationsVisitor = new ClassAnnotationsVisitor()
    classReader.accept(classAnnotationsVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES)

    val exportAnnotations = classAnnotationsVisitor
      .annotations
      .filter(ExportAnnotations.contains(_))

    exportAnnotations.toList match {
      case Nil => ExportDef.Undefined
      case List(annotation) => resultFromAnnotation(className, annotation)
      case l => throw new IllegalArgumentException(s"More than one export annotation on $className: $l")
    }
  }

  private def resultFromAnnotation(className: ClassName, annotation: Type): ExportDef = {
    annotation match {
      case ExportType => {
        logger.debug(s"Found an @Export annotation on ${className.fullName}.")
        ExportDef(ExportSpecificClassesDef(Set(className)))
      }
      case ExportAllType => {
        logger.debug(s"Found an @ExportAll annotation on ${className.fullName}.")
        ExportDef(ExportAllClassesDef, ExportAllPkgsDef)
      }
      case _ => ExportDef.Undefined
    }
  }
}

object AccessDefinitionsReader {
  val ExportType = Type.getType(classOf[Export])
  val ExportAllType = Type.getType(classOf[ExportAll])

  val ExportAnnotations = Set(ExportType, ExportAllType)
}