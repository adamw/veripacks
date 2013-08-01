package org.veripacks.reader

import org.veripacks._
import org.veripacks.reader.dependencies.ClassDependenciesReader
import org.veripacks.reader.accessdefinitions.{AccessDefinitionsAccumulator, SingleClassAccessDefinitionsReader}
import org.veripacks.AccessDefinitionError
import org.veripacks.ClassUsage

@Export
class MetadataReader(classDependenciesReader: ClassDependenciesReader,
                     singleClassAccessDefinitionsReader: SingleClassAccessDefinitionsReader,
                     accessDefinitionsAccumulator: AccessDefinitionsAccumulator) {
  def readUsagesAndAccessDefinitions(pkgs: Iterable[Pkg], classes: Iterable[ClassName]): (Iterable[ClassUsage], Either[List[AccessDefinitionError], AccessDefinitions]) = {
    val classUsages = classes.flatMap { className =>
      val classReader = ClassReaderProducer.create(className)

      val singleClassAccessDefinitions = singleClassAccessDefinitionsReader.readFor(className, classReader)
      accessDefinitionsAccumulator.addSingleClassAccessDefinitions(className.pkg, singleClassAccessDefinitions)

      classDependenciesReader.read(className, classReader, pkgs)
    }

    (classUsages, accessDefinitionsAccumulator.build)
  }
}