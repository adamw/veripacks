package org.veripacks

import org.veripacks.reader.dependencies.ClassDependenciesReader
import org.veripacks.reader.accessdefinitions.{AccessDefinitionsAccumulator, SingleClassAccessDefinitionsReader}
import org.veripacks.reader.MetadataReader
import org.veripacks.verifier.{Verifier, ClassUsageVerifier}

@NotVerified
class VeripacksBuilder {
  lazy val classDependenciesReader = new ClassDependenciesReader()
  lazy val singleClassAccessDefinitionsReader = new SingleClassAccessDefinitionsReader()
  lazy val accessDefinitionsAccumulator = new AccessDefinitionsAccumulator()

  lazy val metadataReader = new MetadataReader(classDependenciesReader,
    singleClassAccessDefinitionsReader,
    accessDefinitionsAccumulator)

  def createClassUsageVerifier(accessDefinitions: AccessDefinitions) = new ClassUsageVerifier(accessDefinitions)
  lazy val verifier = new Verifier(createClassUsageVerifier)

  lazy val veripacks = new Veripacks(metadataReader, verifier)

  def build = veripacks
}

object VeripacksBuilder extends VeripacksBuilder