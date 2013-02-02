package org.veripacks


case class AccessDefinitions(exports: Map[Pkg, ExportDef])

case class ExportDef(classes: ExportClassesDef, pkgs: ExportPkgsDef) {
  def this(classes: ExportClassesDef) = this(classes, ExportPkgsUndefinedDef)
  def this(pkgs: ExportPkgsDef) = this(ExportAllClassesDef, pkgs)
}

object ExportDef {
  def apply(classes: ExportClassesDef): ExportDef = ExportDef(classes, ExportPkgsUndefinedDef)
  def apply(pkgs: ExportPkgsDef): ExportDef = ExportDef(ExportAllClassesDef, pkgs)

  val Undefined = ExportDef(ExportClassesUndefinedDef, ExportPkgsUndefinedDef)
  val All = ExportDef(ExportAllClassesDef, ExportAllPkgsDef)
}

sealed trait ExportClassesDef
case object ExportAllClassesDef extends ExportClassesDef
case object ExportClassesUndefinedDef extends ExportClassesDef
case class ExportSpecificClassesDef(classNames: Set[ClassName]) extends ExportClassesDef

sealed trait ExportPkgsDef
case object ExportAllPkgsDef extends ExportPkgsDef
case object ExportPkgsUndefinedDef extends ExportPkgsDef
case class ExportSpecificPkgsDef(pkgs: Set[Pkg]) extends ExportPkgsDef

case class AccessDefinitionError(msg: String)
