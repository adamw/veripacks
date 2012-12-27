package org.veripacks

case class ClassUsage(cls: ClassName, usedIn: ClassName, detail: ClassUsageDetail)

sealed trait ClassUsageDetail {
  val sourceFileName: String
}

case class LineNumberUsageDetail(sourceFileName: String, lineNumber: Int) extends ClassUsageDetail
case class MethodSignatureUsageDetail(sourceFileName: String, methodName: String) extends ClassUsageDetail
case class MethodBodyUsageDetail(sourceFileName: String, methodName: String, lineNumber: Int) extends ClassUsageDetail
case class FieldUsageDetail(sourceFileName: String, fieldName: String) extends ClassUsageDetail
case class ClassSignatureUsageDetail(sourceFileName: String) extends ClassUsageDetail

case class MultipleUsageDetail(usages: Set[ClassUsageDetail]) extends ClassUsageDetail {
  require(usages.size > 0)
  lazy val sourceFileName: String = usages.iterator.next().sourceFileName // All source file names should be the same
}