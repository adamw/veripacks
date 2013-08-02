package org.veripacks

import org.veripacks.reader.dependencies.ClassDependenciesReader
import org.veripacks.reader.accessdefinitions.{AccessDefinitionsAccumulator, ClassAccessDefinitionsReader}
import org.veripacks.reader.MetadataReader
import org.veripacks.verifier.{Verifier, ClassUsageVerifier}
import scala.collection.JavaConverters._

@NotVerified
trait VeripacksBuilder {
  lazy val classDependenciesReader = new ClassDependenciesReader(requireImportFilter)
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
  def requireImportFilter: ClassNameFilter
}

object VeripacksBuilder {
  private var _customAccessDefinitionsReader: CustomAccessDefinitionsReader = NoOpCustomAccessDefinitionsReader
  private var _requireImportFilter: ClassNameFilter = AllUnknownClassNameFilter

  def withCustomAccessDefinitionReader(newCustomAccessDefinitionsReader: CustomAccessDefinitionsReader) = {
    _customAccessDefinitionsReader = newCustomAccessDefinitionsReader
    this
  }

  def requireImportOf(packagePrefix: String): this.type = {
    requireImportOf(List(packagePrefix))
    this
  }

  def requireImportOf(packagePrefixes: java.util.Collection[String]): this.type = {
    requireImportOf(packagePrefixes.asScala)
    this
  }

  def requireImportOf(packagePrefixes: Iterable[String]): this.type = {
    _requireImportFilter = _requireImportFilter.or(IncludeClassNameFilter(packagePrefixes))
    this
  }

  def doNotRequireImportOf(packagePrefix: String): this.type = {
    doNotRequireImportOf(List(packagePrefix))
    this
  }

  def doNotRequireImportOf(packagePrefixes: java.util.Collection[String]): this.type = {
    doNotRequireImportOf(packagePrefixes.asScala)
    this
  }

  def doNotRequireImportOf(packagePrefixes: Iterable[String]): this.type = {
    _requireImportFilter = _requireImportFilter.or(ExcludeClassNameFilter(packagePrefixes))
    this
  }

  def build = new VeripacksBuilder {
    def customAccessDefinitionsReader = _customAccessDefinitionsReader
    def requireImportFilter = _requireImportFilter
  }.veripacks
}