package org.veripacks.reader

import collection.mutable
import java.net.{URL, URLDecoder}
import java.util.jar.JarFile
import java.io.File
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.JavaConverters._
import org.veripacks.{RootPkg, Pkg, ClassName}

class ClassNamesLister extends Logging {
  // http://stackoverflow.com/questions/1456930/how-do-i-read-all-classes-from-a-java-package-in-the-classpath/7461653#7461653
  def list(pkg: Pkg): Iterable[ClassName] = {
    val classLoader = Thread.currentThread().getContextClassLoader
    val names = new mutable.HashSet[ClassName]()

    val packageNameSlashed = pkg.name.replace('.', '/')
    val packageURLs = classLoader.getResources(packageNameSlashed)

    for (packageURL <- packageURLs.asScala) {
      logger.debug(s"Processing package URL $packageURL")
      val protocol = packageURL.getProtocol

      if (protocol.equals("jar")) {
        names ++= allClassesInJar(packageURL, packageNameSlashed)
      } else if (protocol.equals("file")) {
        names ++= allClassesInDirectory(pkg, new File(packageURL.getFile))
      } else {
        throw new IllegalArgumentException(s"Unsupported protocol $protocol in URL $packageURL for package $pkg.")
      }
    }

    names
  }

  private def allClassesInJar(packageURL: URL, packageNameSlashed: String) = {
    val names = mutable.HashSet[ClassName]()

    val protocolLength = packageURL.getProtocol.length + 2 // we have to account for :/
    // build jar file name, then loop through zipped entries
    val jarFileNameWithProtocol = URLDecoder.decode(packageURL.getFile, "UTF-8")
    val jarFileName = jarFileNameWithProtocol.substring(protocolLength, jarFileNameWithProtocol.indexOf("!"))
    val jf = new JarFile(jarFileName)
    val jarEntries = jf.entries()
    while (jarEntries.hasMoreElements) {
      val entryNameSlashed = jarEntries.nextElement().getName
      if (entryNameSlashed.startsWith(packageNameSlashed)) {
        addEntryIfClass(names, RootPkg, entryNameSlashed)
      }
    }

    names.toSet
  }

  private def allClassesInDirectory(pkg: Pkg, dir: File): Set[ClassName] = {
    val names = mutable.HashSet[ClassName]()
    val content = dir.listFiles
    for (actual <- content) {
      val entryName = actual.getName
      val added = addEntryIfClass(names, pkg, entryName)
      if (!added) {
        val entryFile = new File(dir, entryName)
        if (entryFile.isDirectory) {
          names ++= allClassesInDirectory(pkg.child(entryName), entryFile)
        } else {
          logger.debug(s"Not a class or a directory: $pkg for package $entryName")
        }
      }
    }

    names.toSet
  }

  /**
   * @return True if the entry was a class and hence was added.
   */
  private def addEntryIfClass(names: mutable.Set[ClassName], rootPkg: Pkg, entryNameSlashed: String) = {
    if (isClass(entryNameSlashed)) {
      val entryNameSlashedWithoutDotClass = entryNameSlashed.substring(0, entryNameSlashed.length - 6)
      val entryNameDottedWithoutDotClass = entryNameSlashedWithoutDotClass.replace('/', '.')

      // The entry name may contain some part of the package
      val lastDot = entryNameDottedWithoutDotClass.lastIndexOf('.')
      val (pkg, className) = if (lastDot == -1) {
        (rootPkg, entryNameDottedWithoutDotClass)
      } else {
        (rootPkg.child(entryNameDottedWithoutDotClass.substring(0, lastDot)), entryNameDottedWithoutDotClass.substring(lastDot+1))
      }
      val toAdd = ClassName(pkg, className)
      names += toAdd
      logger.debug(s"Adding class $toAdd")
      true
    } else {
      false
    }
  }

  private def isClass(entryName: String) = entryName.endsWith(".class")
}
