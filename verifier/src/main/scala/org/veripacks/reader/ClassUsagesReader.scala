package org.veripacks.reader

import org.veripacks._
import javassist.{CtMethod, CtClass, CtField, ClassPool}
import com.typesafe.scalalogging.slf4j.Logging
import collection.mutable.ListBuffer
import org.veripacks.ClassName
import org.veripacks.ClassUsage

class ClassUsagesReader extends Logging {
  private val classPool = ClassPool.getDefault

  def read(usagesIn: ClassName, scope: Pkg): Iterable[ClassUsage] = {
    logger.debug(s"Reading usages in $usagesIn.")

    val usagesAccumulator = new UsagesAccumulator(classPool.get(usagesIn.fullName), usagesIn, scope)
    usagesAccumulator.read()

    combineSameUsages(usagesAccumulator.usages.toList)
  }

  def combineSameUsages(raw: List[ClassUsage]) = {
    raw.groupBy(cu => (cu.cls, cu.usedIn)).values.map(cuList => {
      ClassUsage(cuList(0).cls, cuList(0).usedIn, MultipleUsageDetail(cuList.map(_.detail).toSet))
    })
  }

  private class UsagesAccumulator(usagesIn: CtClass, usagesInClassName: ClassName, scope: Pkg) {
    val usages = new ListBuffer[ClassUsage]()

    private val sourceFileName = usagesIn.getClassFile.getSourceFile

    def read() {
      readFromFields(usagesIn.getDeclaredFields)
      readFromMethods(usagesIn.getDeclaredMethods)
    }

    private def readFromFields(fields: Array[CtField]) {
      for (field <- fields) { readFromField(field) }
    }

    private def readFromField(field: CtField) {
      possibleUsage(field.getType, FieldUsageDetail(sourceFileName, field.getName))
    }

    private def readFromMethods(methods: Array[CtMethod]) {
      for (method <- methods) { readFromMethod(method) }
    }

    private def readFromMethod(method: CtMethod) {
      val methodUsageDetail = MethodSignatureUsageDetail(sourceFileName, method.getName, method.getMethodInfo.getLineNumber(0))

      possibleUsage(method.getReturnType, methodUsageDetail)

      for (parameterType <- method.getParameterTypes) {
        possibleUsage(parameterType, methodUsageDetail)
      }
    }

    private def possibleUsage(ctClass: CtClass, usageDetail: ClassUsageDetail) {
      if (!ctClass.isPrimitive && ctClass.getPackageName.startsWith(scope.name)) {
        val usage = ClassUsage(ClassName(Pkg(ctClass.getPackageName), ctClass.getSimpleName),
          usagesInClassName, usageDetail)
        logger.debug(s"Adding usage $usage.")
        usages += usage
      }
    }
  }
}
