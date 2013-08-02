package org.veripacks

import org.veripacks.reader.dependencies.ClassDependenciesReader
import org.veripacks.reader.accessdefinitions.{AccessDefinitionsAccumulator, ClassAccessDefinitionsReader}
import org.veripacks.reader.MetadataReader
import org.veripacks.verifier.{Verifier, ClassUsageVerifier}
import scala.collection.JavaConverters._

@NotVerified
trait VeripacksBuilder {
  lazy val classDependenciesReader = new ClassDependenciesReader(classNameFilter)
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
  def classNameFilter: ClassNameFilter
}

object VeripacksBuilder {
  private var _customAccessDefinitionsReader: CustomAccessDefinitionsReader = NoOpCustomAccessDefinitionsReader
  private var _classNameFilter: ClassNameFilter = AllUnknownClassNameFilter

  def withCustomAccessDefinitionReader(newCustomAccessDefinitionsReader: CustomAccessDefinitionsReader) = {
    _customAccessDefinitionsReader = newCustomAccessDefinitionsReader
    this
  }

  def checkUsagesOfClassesFrom(packagePrefix: String): this.type = {
    checkUsagesOfClassesFrom(List(packagePrefix))
    this
  }

  def checkUsagesOfClassesFrom(packagePrefixes: java.util.Collection[String]): this.type = {
    checkUsagesOfClassesFrom(packagePrefixes.asScala)
    this
  }

  def checkUsagesOfClassesFrom(packagePrefixes: Iterable[String]): this.type = {
    _classNameFilter = _classNameFilter.or(IncludeClassNameFilter(packagePrefixes))
    this
  }

  def doNotCheckUsagesOfClassesFrom(packagePrefix: String): this.type = {
    doNotCheckUsagesOfClassesFrom(List(packagePrefix))
    this
  }

  def doNotCheckUsagesOfClassesFrom(packagePrefixes: java.util.Collection[String]): this.type = {
    doNotCheckUsagesOfClassesFrom(packagePrefixes.asScala)
    this
  }

  def doNotCheckUsagesOfClassesFrom(packagePrefixes: Iterable[String]): this.type = {
    _classNameFilter = _classNameFilter.or(ExcludeClassNameFilter(packagePrefixes))
    this
  }

  def build = new VeripacksBuilder {
    def customAccessDefinitionsReader = _customAccessDefinitionsReader
    def classNameFilter = _classNameFilter
  }.veripacks
}