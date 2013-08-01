package org.veripacks

import org.veripacks.verifier.ClassUsageVerifierResult

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
