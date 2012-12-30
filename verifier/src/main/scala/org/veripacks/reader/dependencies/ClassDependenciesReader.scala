package org.veripacks.reader.dependencies

import com.typesafe.scalalogging.slf4j.Logging
import collection.mutable.ListBuffer
import org.objectweb.asm._
import org.veripacks.{Pkg, Export, ClassName, ClassUsage}

@Export
class ClassDependenciesReader extends Logging {
  def read(dependenciesOf: ClassName, classReader: ClassReader, scope: Iterable[Pkg]): Iterable[ClassUsage] = {
    def inScope(className: ClassName) = {
      scope.exists(className.pkg.isSubpackageOf(_))
    }

    logger.debug(s"Reading dependencies of $dependenciesOf with scope $scope.")

    val sourceFileNameVisitor = new SourceFileNameVisitor
    classReader.accept(sourceFileNameVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES)

    logger.debug(s"Determined source file name ${sourceFileNameVisitor.sourceFileName}.")

    val dependencyVisitor = new DependencyVisitor(sourceFileNameVisitor.sourceFileName)
    classReader.accept(dependencyVisitor, 0)

    val usages = ListBuffer[ClassUsage]()
    for ((className, classUsageDetail) <- dependencyVisitor.usages) {
      if (inScope(className) && className != dependenciesOf) {
        usages += ClassUsage(className, dependenciesOf, classUsageDetail)
      }
    }

    usages.toList
  }
}
