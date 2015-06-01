package org.veripacks.reader.dependencies

import org.objectweb.asm.{Opcodes, ClassVisitor}

class SourceFileNameVisitor extends ClassVisitor(Opcodes.ASM5) {
  var sourceFileName: String = _

  override def visitSource(source: String, debug: String) {
    sourceFileName = source
  }
}
