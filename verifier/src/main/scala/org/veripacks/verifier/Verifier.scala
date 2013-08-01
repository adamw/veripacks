package org.veripacks.verifier

import org.veripacks._
import org.veripacks.verifier.ClassUsageVerifierResult.Allowed
import org.veripacks.ClassUsage
import org.veripacks.VerifyResultBrokenConstraints
import scala.Some
import org.veripacks.AccessDefinitions

@Export
class Verifier {
  def doVerify(classUsages: Iterable[ClassUsage], accessDefinitions: AccessDefinitions) = {
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
