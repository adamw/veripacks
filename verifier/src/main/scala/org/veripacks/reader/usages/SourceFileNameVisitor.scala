package org.veripacks.reader.usages

import org.objectweb.asm.{Opcodes, ClassVisitor}

class SourceFileNameVisitor extends ClassVisitor(Opcodes.ASM4) {
  var sourceFileName: String = _

  override def visitSource(source: String, debug: String) {
    sourceFileName = source
  }
}
