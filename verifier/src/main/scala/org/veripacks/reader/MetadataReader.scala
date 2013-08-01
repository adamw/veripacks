package org.veripacks.reader

import org.veripacks._
import org.veripacks.reader.dependencies.ClassDependenciesReader
import org.veripacks.reader.accessdefinitions.{AccessDefinitionsAccumulator, ClassAccessDefinitionsReader}

@Export
class MetadataReader(classDependenciesReader: ClassDependenciesReader,
                     singleClassAccessDefinitionsReader: ClassAccessDefinitionsReader,
                     accessDefinitionsAccumulator: AccessDefinitionsAccumulator,
                     customAccessDefinitionsReader: CustomAccessDefinitionsReader) {
  def readUsagesAndAccessDefinitions(pkgs: Iterable[Pkg], classes: Iterable[ClassName]): Metadata = {
    val notVerified = scala.collection.mutable.HashSet[ClassName]()

    val classUsages = classes.flatMap { className =>
      val classReader = ClassReaderProducer.create(className)

      // Default
      val singleClassAccessDefinitions = singleClassAccessDefinitionsReader.readFor(className, classReader)
      accessDefinitionsAccumulator.addSingleClassAccessDefinitions(className.pkg, singleClassAccessDefinitions)

      // Custom
      val customClassAccessDefinitions = customAccessDefinitionsReader.forClass(className)
      accessDefinitionsAccumulator.addSingleClassAccessDefinitions(className.pkg, customClassAccessDefinitions)

      if (!singleClassAccessDefinitions.verified || !customClassAccessDefinitions.verified) {
        notVerified += className
      }

      classDependenciesReader.read(className, classReader, pkgs)
    }

    Metadata(classUsages, accessDefinitionsAccumulator.build, notVerified.toSet)
  }
}

@Export
case class Metadata(classUsages: Iterable[ClassUsage],
                    accessDefinitionsOrErrors: Either[List[AccessDefinitionError], AccessDefinitions],
                    notVerified: Set[ClassName])