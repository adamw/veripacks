package org.veripacks

import org.veripacks.reader.ClassNamesLister
import org.veripacks.reader.dependencies.ClassDependenciesReader
import org.veripacks.reader.accessdefinitions.{SingleClassAccessDefinitionsReader, AccessDefinitionsAccumulator}
import com.typesafe.scalalogging.slf4j.Logging
import org.veripacks.classusageverifier.{ClassUsageVerifierResult, ClassUsageVerifier}
import org.veripacks.classusageverifier.ClassUsageVerifierResult.Allowed

class Verifier extends Logging {
  def verify(rootPackage: String): VerifyResult = verify(List(rootPackage))

  def verify(rootPackages: java.util.Collection[String]): VerifyResult = {
    import scala.collection.JavaConverters._
    verify(rootPackages.asScala)
  }

  def verify(rootPackages: Iterable[String]): VerifyResult = {
    val pkgs = rootPackages.map(Pkg(_))
    val classes = listClassesFromAllPackages(pkgs)

    logger.info(s"Checking ${pkgs.size} packages, containing ${classes.size} classes.")

    val (classUsages, accessDefinitionsOrErrors) = readUsagesAndAccessDefinitions(pkgs, classes)

    accessDefinitionsOrErrors match {
      case Left(errors) => VerifyResultAccessDefinitionError(errors)
      case Right(accessDefinitions) => doVerify(classUsages, accessDefinitions)
    }
  }

  private def listClassesFromAllPackages(pkgs: Iterable[Pkg]) = {
    val classNamesLister = new ClassNamesLister()
    pkgs.flatMap(classNamesLister.list(_))
  }

  private def readUsagesAndAccessDefinitions(pkgs: Iterable[Pkg], classes: Iterable[ClassName]) = {
    val classDependenciesReader = new ClassDependenciesReader()
    val singleClassAccessDefinitionsReader = new SingleClassAccessDefinitionsReader()
    val accessDefinitionsAccumulator = new AccessDefinitionsAccumulator()

    val classUsages = classes.flatMap { className =>
      val classReader = ClassReaderProducer.create(className)

      val singleClassAccessDefinitions = singleClassAccessDefinitionsReader.readFor(className, classReader)
      accessDefinitionsAccumulator.addSingleClassAccessDefinitions(className.pkg, singleClassAccessDefinitions)

      classDependenciesReader.read(className, classReader, pkgs)
    }

    (classUsages, accessDefinitionsAccumulator.build)
  }

  private def doVerify(classUsages: Iterable[ClassUsage], accessDefinitions: AccessDefinitions) = {
    val classUsageVerifier = new ClassUsageVerifier(accessDefinitions)
    val forbiddenUsages = classUsages.flatMap(classUsage => {
      val result = classUsageVerifier.verify(classUsage)
      if (result == Allowed) None else Some((classUsage, result))
    })

    if (forbiddenUsages.size == 0) {
      VerifyResultOk
    } else {
      VerifyResultBrokenConstraints(forbiddenUsages.toList)
    }
  }
}

sealed trait VerifyResult {
  def toOption: Option[String]

  def throwIfNotOk() {
    toOption.foreach(msg => throw new VerificationException(msg))
  }
}

case object VerifyResultOk extends VerifyResult {
  def toOption = None
}

case class VerifyResultBrokenConstraints(brokenConstraints: List[(ClassUsage, ClassUsageVerifierResult)]) extends VerifyResult {
  def toOption = {
    val desc = brokenConstraints.map { case (classUsage, classUsageVerifierResult) => {
      s"Class: ${classUsage.cls.fullName} cannot be used in ${classUsage.usedIn.fullName} (${classUsage.detail}), " +
        s"because of: $classUsageVerifierResult."
    } }.mkString("\n")

    Some(s"Broken constraints:\n$desc")
  }
}

case class VerifyResultAccessDefinitionError(accessDefinitionErrors: List[AccessDefinitionError]) extends VerifyResult {
  def toOption = {
    val desc = accessDefinitionErrors.map(_.msg).mkString("\n")

    Some(s"Access definition errors:\n$desc")
  }
}

class VerificationException(description: String) extends RuntimeException(description)
