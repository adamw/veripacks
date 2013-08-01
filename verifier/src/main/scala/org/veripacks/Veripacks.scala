package org.veripacks

import org.veripacks.reader.{MetadataReader, ClassNamesLister}
import com.typesafe.scalalogging.slf4j.Logging
import org.veripacks.verifier.Verifier

class Veripacks(metadataReader: MetadataReader, verifier: Verifier) extends Logging {
  def verify(rootPackage: String): VerifyResult = verify(List(rootPackage))

  def verify(rootPackages: java.util.Collection[String]): VerifyResult = {
    import scala.collection.JavaConverters._
    verify(rootPackages.asScala)
  }

  def verify(rootPackages: Iterable[String]): VerifyResult = {
    val pkgs = rootPackages.map(Pkg(_))
    val classes = listClassesFromAllPackages(pkgs)

    logger.info(s"Checking ${pkgs.size} packages, containing ${classes.size} classes.")

    val (classUsages, accessDefinitionsOrErrors) = metadataReader.readUsagesAndAccessDefinitions(pkgs, classes)

    accessDefinitionsOrErrors match {
      case Left(errors) => VerifyResultAccessDefinitionError(errors)
      case Right(accessDefinitions) => verifier.doVerify(classUsages, accessDefinitions)
    }
  }

  private def listClassesFromAllPackages(pkgs: Iterable[Pkg]) = {
    val classNamesLister = new ClassNamesLister()
    pkgs.flatMap(classNamesLister.list)
  }
}
