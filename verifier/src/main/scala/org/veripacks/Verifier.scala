package org.veripacks

import org.veripacks.reader.ClassNamesLister
import org.veripacks.reader.dependencies.ClassDependenciesReader
import org.veripacks.reader.accessdefinitions.{AccessDefinitionsReader, AccessDefinitionsAccumulator}
import org.objectweb.asm.ClassReader

class Verifier {
  def verify(rootPackage: String): VerifyResult = verify(List(rootPackage))

  def verify(rootPackages: java.util.Collection[String]): VerifyResult = {
    import scala.collection.JavaConverters._
    verify(rootPackages.asScala)
  }

  def verify(rootPackages: Iterable[String]): VerifyResult = {
    val pkgs = rootPackages.map(Pkg(_))
    val classes = listClassesFromAllPackages(pkgs)
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
    val accessDefinitionsReader = new AccessDefinitionsReader()
    val accessDefinitionsAccumulator = new AccessDefinitionsAccumulator()

    val classUsages = classes.flatMap { className =>
      val classReader = new ClassReader(className.fullName)

      val exportDefinition = accessDefinitionsReader.readFor(className, classReader)
      accessDefinitionsAccumulator.addExportDefinition(className.pkg, exportDefinition)

      classDependenciesReader.read(className, classReader, pkgs)
    }

    (classUsages, accessDefinitionsAccumulator.build)
  }

  private def doVerify(classUsages: Iterable[ClassUsage], accessDefinitions: AccessDefinitions) = {
    val classUsageVerifier = new ClassUsageVerifier(accessDefinitions)
    val forbiddenUsages = classUsages.filter(!classUsageVerifier.isAllowed(_))

    if (forbiddenUsages.size == 0) {
      VerifyResultOk
    } else {
      VerifyResultBrokConstraints(forbiddenUsages.toList)
    }
  }
}

sealed trait VerifyResult {
  def throwIfNotOk
}

case object VerifyResultOk extends VerifyResult {
  def throwIfNotOk {}
}

case class VerifyResultBrokConstraints(brokenConstraints: List[ClassUsage]) extends VerifyResult {
  def throwIfNotOk {
    val desc = brokenConstraints.mkString("\n")
    throw new VerificationException(s"Broken constraints:\n$desc")
  }
}

case class VerifyResultAccessDefinitionError(accessDefinitionErrors: List[AccessDefinitionError]) extends VerifyResult {
  def throwIfNotOk {
    val desc = accessDefinitionErrors.map(_.msg).mkString("\n")
    throw new VerificationException(s"Access definition errors:\n$desc")
  }
}

class VerificationException(description: String) extends RuntimeException(description)
