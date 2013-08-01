package org.veripacks.reader

import org.veripacks._
import org.veripacks.reader.dependencies.ClassDependenciesReader
import org.veripacks.reader.accessdefinitions.{AccessDefinitionsAccumulator, SingleClassAccessDefinitionsReader}

@Export
class MetadataReader(classDependenciesReader: ClassDependenciesReader,
                     singleClassAccessDefinitionsReader: SingleClassAccessDefinitionsReader,
                     accessDefinitionsAccumulator: AccessDefinitionsAccumulator) {
  def readUsagesAndAccessDefinitions(pkgs: Iterable[Pkg], classes: Iterable[ClassName]): Metadata = {
    val notVerified = scala.collection.mutable.HashSet[ClassName]()

    val classUsages = classes.flatMap { className =>
      val classReader = ClassReaderProducer.create(className)

      val singleClassAccessDefinitions = singleClassAccessDefinitionsReader.readFor(className, classReader)
      accessDefinitionsAccumulator.addSingleClassAccessDefinitions(className.pkg, singleClassAccessDefinitions)

      if (!singleClassAccessDefinitions.verified) {
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