package org.veripacks.reader

import org.veripacks.{Pkg, ClassName, ClassUsage}
import javassist.{CtMethod, CtClass, CtField, ClassPool}
import com.typesafe.scalalogging.slf4j.Logging
import collection.mutable.ListBuffer
import javax.management.remote.rmi._RMIConnection_Stub

class ClassUsagesReader extends Logging {
  private val classPool = ClassPool.getDefault

  def read(usagesIn: ClassName, scope: Pkg): List[ClassUsage] = {
    logger.debug(s"Reading usages in $usagesIn.")

    val usagesAccumulator = new UsagesAccumulator(usagesIn, scope)
    usagesAccumulator.readFromClass(classPool.get(usagesIn.fullName))

    usagesAccumulator.usages.toList
  }

  private class UsagesAccumulator(usagesIn: ClassName, scope: Pkg) {
    val usages = new ListBuffer[ClassUsage]()

    def readFromClass(ctClass: CtClass) {
      readFromFields(ctClass.getFields)
      readFromMethods(ctClass.getDeclaredMethods)
    }

    private def readFromFields(fields: Array[CtField]) {
      for (field <- fields) { readFromField(field) }
    }

    private def readFromField(field: CtField) {
      // TODO
    }

    private def readFromMethods(methods: Array[CtMethod]) {
      for (method <- methods) { readFromMethod(method) }
    }

    private def readFromMethod(method: CtMethod) {
      possibleUsage(method.getReturnType, method.getMethodInfo.getLineNumber(0))
    }

    private def possibleUsage(ctClass: CtClass, lineNumber: Int) {
      if (ctClass.getPackageName.startsWith(scope.name)) {
        val usage = ClassUsage(ClassName(Pkg.from(ctClass.getPackageName), ctClass.getSimpleName), usagesIn, lineNumber)
        logger.debug(s"Adding usage $usage.")
        usages += usage
      }
    }
  }
}
