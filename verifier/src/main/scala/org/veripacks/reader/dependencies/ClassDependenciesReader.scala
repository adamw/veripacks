package org.veripacks.reader.dependencies

import com.typesafe.scalalogging.slf4j.Logging
import collection.mutable.ListBuffer
import org.objectweb.asm._
import org.veripacks._
import org.veripacks.ClassUsage

@Export
class ClassDependenciesReader(classUsageFilter: ClassUsageFilter) extends Logging {
  def read(dependenciesOf: ClassName, classReader: ClassReader, scope: Iterable[Pkg]): Iterable[ClassUsage] = {
    def inScope(classUsage: ClassUsage) = {
      scope.exists(classUsage.cls.pkg.isChildPackageOf) ||
        (classUsageFilter.shouldBeVerified(classUsage) == ClassUsageFilter.Yes)
    }

    logger.debug(s"Reading dependencies of $dependenciesOf with scope $scope.")

    val sourceFileNameVisitor = new SourceFileNameVisitor
    classReader.accept(sourceFileNameVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES)

    logger.debug(s"Determined source file name ${sourceFileNameVisitor.sourceFileName}.")

    val dependencyVisitor = new DependencyVisitor(sourceFileNameVisitor.sourceFileName)
    classReader.accept(dependencyVisitor, 0)

    val usages = ListBuffer[ClassUsage]()
    for ((className, classUsageDetail) <- dependencyVisitor.usages) {
      val classUsage = ClassUsage(className, dependenciesOf, classUsageDetail)
      if (inScope(classUsage) && className != dependenciesOf) {
        usages += classUsage
      }
    }

    usages.toList
  }
}
