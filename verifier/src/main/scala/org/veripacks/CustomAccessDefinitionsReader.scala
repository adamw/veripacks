package org.veripacks

trait CustomAccessDefinitionsReader {
  def readExportDefs(className: ClassName): List[ExportDef] = List(ExportDef(ExportClassesUndefinedDef, ExportPkgsUndefinedDef))
  def readImportDef(pkg: Pkg): ImportDef = ImportDef(Set())
  def isRequiresImport(pkg: Pkg): Boolean = false
  def isVerified(className: ClassName): Boolean = true

  def forClass(className: ClassName) = {
    val pkg = className.pkg
    ClassAccessDefinitions(readExportDefs(className), readImportDef(pkg), isRequiresImport(pkg),
      isVerified(className))
  }
}

object NoOpCustomAccessDefinitionsReader extends CustomAccessDefinitionsReader