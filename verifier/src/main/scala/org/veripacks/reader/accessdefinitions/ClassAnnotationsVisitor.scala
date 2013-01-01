package org.veripacks.reader.accessdefinitions

import org.objectweb.asm.{Type, Opcodes, ClassVisitor}
import org.veripacks.Export

class ClassAnnotationsVisitor extends ClassVisitor(Opcodes.ASM4) {
  val annotations = collection.mutable.HashSet[Type]()

  override def visitAnnotation(desc: String, visible: Boolean) = {
    val `type` = Type.getType(desc)
    annotations.add(`type`)

    null
  }
}
