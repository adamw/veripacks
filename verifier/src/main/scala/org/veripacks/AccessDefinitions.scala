package org.veripacks

case class AccessDefinitions(exports: Map[Pkg, ExportDef], imports: Map[Pkg, ImportDef], requiresImport: Set[Pkg])

case class ExportDef(classes: ExportClassesDef, pkgs: ExportPkgsDef) {
  def this(classes: ExportClassesDef) = this(classes, ExportPkgsUndefinedDef)
  def this(pkgs: ExportPkgsDef) = this(ExportAllClassesDef, pkgs)

  def allClassesExported = classes == ExportAllClassesDef || this == ExportDef.Undefined
  def allPkgsExported = pkgs == ExportAllPkgsDef || this == ExportDef.Undefined
}

object ExportDef {
  def apply(classes: ExportClassesDef): ExportDef = ExportDef(classes, ExportPkgsUndefinedDef)
  def apply(pkgs: ExportPkgsDef): ExportDef = ExportDef(ExportClassesUndefinedDef, pkgs)

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

case class ImportDef(pkgs: Set[Pkg])

case class AccessDefinitionError(msg: String)
