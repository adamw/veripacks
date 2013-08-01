package org.veripacks

import org.veripacks.reader.dependencies.ClassDependenciesReader
import org.veripacks.reader.accessdefinitions.{AccessDefinitionsAccumulator, ClassAccessDefinitionsReader}
import org.veripacks.reader.MetadataReader
import org.veripacks.verifier.{Verifier, ClassUsageVerifier}

@NotVerified
trait VeripacksBuilder {
  lazy val classDependenciesReader = new ClassDependenciesReader()
  lazy val singleClassAccessDefinitionsReader = new ClassAccessDefinitionsReader()
  lazy val accessDefinitionsAccumulator = new AccessDefinitionsAccumulator()

  lazy val metadataReader = new MetadataReader(classDependenciesReader,
    singleClassAccessDefinitionsReader,
    accessDefinitionsAccumulator,
    customAccessDefinitionsReader)

  def createClassUsageVerifier(accessDefinitions: AccessDefinitions) = new ClassUsageVerifier(accessDefinitions)
  lazy val verifier = new Verifier(createClassUsageVerifier)

  lazy val veripacks = new Veripacks(metadataReader, verifier)

  def customAccessDefinitionsReader: CustomAccessDefinitionsReader
}

object VeripacksBuilder {
  private var _customAccessDefinitionsReader: CustomAccessDefinitionsReader = NoOpCustomAccessDefinitionsReader

  def withCustomAccessDefinitionReader(newCustomAccessDefinitionsReader: CustomAccessDefinitionsReader) = {
    _customAccessDefinitionsReader = newCustomAccessDefinitionsReader
    this
  }

  def build = new VeripacksBuilder {
    def customAccessDefinitionsReader = _customAccessDefinitionsReader
  }.veripacks
}