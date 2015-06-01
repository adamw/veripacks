package org.veripacks.reader.accessdefinitions

import org.objectweb.asm.{AnnotationVisitor, Type, Opcodes, ClassVisitor}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class ClassAnnotationsVisitor extends ClassVisitor(Opcodes.ASM5) {
  val annotationsWithValues = mutable.HashMap[Type, mutable.HashMap[String, Any]]()

  override def visitAnnotation(desc: String, visible: Boolean) = {
    val `type` = Type.getType(desc)
    val values = new mutable.HashMap[String, Any]
    annotationsWithValues.put(`type`, values)

    new AnnotationValuesVisitor(values)
  }
}

class AnnotationValuesVisitor(values: mutable.Map[String, Any]) extends AnnotationVisitor(Opcodes.ASM5) {
  override def visit(name: String, value: Any) {
    values(name) = value
  }

  override def visitArray(name: String) = {
    val arrayValues = ListBuffer[Any]()
    values(name) = arrayValues
    new AnnotationArrayValuesVisitor(arrayValues)
  }
}

class AnnotationArrayValuesVisitor(arrayValues: ListBuffer[Any]) extends AnnotationVisitor(Opcodes.ASM5) {
  override def visit(name: String, value: Any) {
    arrayValues += value
  }
}