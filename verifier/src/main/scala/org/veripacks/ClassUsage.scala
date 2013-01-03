package org.veripacks

case class ClassUsage(cls: ClassName, usedIn: ClassName, detail: ClassUsageDetail) {
  override def toString = s"Class: ${cls.fullName} cannot be used in ${usedIn.fullName}. Class usage details: $detail."
}

sealed trait ClassUsageDetail {
  val sourceFileName: String
}

case class MethodSignatureUsageDetail(sourceFileName: String, methodName: String) extends ClassUsageDetail {
  override def toString = s"$methodName signature in $sourceFileName"
}

case class MethodBodyUsageDetail(sourceFileName: String, methodName: String, lineNumber: Int) extends ClassUsageDetail {
  override def toString = s"line $lineNumber of $methodName method in $sourceFileName"
}

case class FieldUsageDetail(sourceFileName: String, fieldName: String) extends ClassUsageDetail {
  override def toString = s"$fieldName field in $sourceFileName"
}

case class ClassSignatureUsageDetail(sourceFileName: String) extends ClassUsageDetail {
  override def toString = s"class signature in $sourceFileName"
}

case class MultipleUsageDetail(usages: Set[ClassUsageDetail]) extends ClassUsageDetail {
  require(usages.size > 0)
  lazy val sourceFileName: String = usages.iterator.next().sourceFileName // All source file names should be the same

  override def toString = usages.mkString("; ")
}