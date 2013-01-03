package org.veripacks

import org.objectweb.asm.ClassReader

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
