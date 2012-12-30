package org.veripacks

case class AccessDefinitions(exports: Map[Pkg, ExportDefinition]) {

}

sealed trait ExportDefinition
case object ExportAllDefinition extends ExportDefinition
case object ExportUndefinedDefinition extends ExportDefinition
case class ExportClassesDefinition(classNames: Set[ClassName]) extends ExportDefinition
