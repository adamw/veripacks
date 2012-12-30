package org.veripacks.reader.usages

import org.veripacks._
import com.typesafe.scalalogging.slf4j.Logging
import collection.mutable.ListBuffer
import org.veripacks.ClassName
import org.veripacks.ClassUsage
import org.objectweb.asm._
import org.objectweb.asm.signature.SignatureVisitor
import org.veripacks.ClassName
import org.veripacks.ClassUsage

@Export
class ClassUsagesReader extends Logging {
  def read(usagesIn: ClassName, classReader: ClassReader, scope: Iterable[Pkg]): Iterable[ClassUsage] = {
    def inScope(className: ClassName) = {
      scope.exists(className.pkg.isSubpackageOf(_))
    }

    logger.debug(s"Reading usages in $usagesIn with scope $scope.")

    val sourceFileNameVisitor = new SourceFileNameVisitor
    classReader.accept(sourceFileNameVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES)

    logger.debug(s"Determined source file name ${sourceFileNameVisitor.sourceFileName}.")

    val dependencyVisitor = new DependencyVisitor(sourceFileNameVisitor.sourceFileName)
    classReader.accept(dependencyVisitor, 0)

    val usages = ListBuffer[ClassUsage]()
    for ((className, classUsageDetail) <- dependencyVisitor.usages) {
      if (inScope(className) && className != usagesIn) {
        usages += ClassUsage(className, usagesIn, classUsageDetail)
      }
    }

    usages.toList
  }
}
