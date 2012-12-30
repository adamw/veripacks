package org.veripacks.reader

import org.veripacks.{ExportUndefinedDefinition, ExportDefinition, ClassName}
import org.objectweb.asm.ClassReader

class AccessDefinitionsReader {
  def readFor(className: ClassName, classReader: ClassReader): ExportDefinition = {
    ExportUndefinedDefinition
  }
}
