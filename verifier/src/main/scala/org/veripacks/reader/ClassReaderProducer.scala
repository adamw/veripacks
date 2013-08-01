package org.veripacks.reader

import org.objectweb.asm.ClassReader
import org.veripacks.ClassName

object ClassReaderProducer {
  def create(className: ClassName) = {
    val resourceName = className.fullName.replace('.', '/') + ".class"
    val resourceInputStream = this.getClass.getClassLoader.getResourceAsStream(resourceName)
    try {
      new ClassReader(resourceInputStream)
    } finally {
      resourceInputStream.close()
    }
  }
}
