package org.veripacks.reader

import collection.mutable
import java.net.{URL, URLDecoder}
import java.util.jar.JarFile
import java.io.File
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.JavaConverters._
import org.veripacks.ClassName

class ClassNamesLister extends Logging {
  // http://stackoverflow.com/questions/1456930/how-do-i-read-all-classes-from-a-java-package-in-the-classpath/7461653#7461653
  def list(packageName: String): Iterable[ClassName] = {
    val classLoader = Thread.currentThread().getContextClassLoader
    val names = new mutable.HashSet[ClassName]()

    val packageNameSlashed = packageName.replace('.', '/')
    val packageURLs = classLoader.getResources(packageNameSlashed)

    for (packageURL <- packageURLs.asScala) {
      logger.debug(s"Processing package URL $packageURL")
      val protocol = packageURL.getProtocol

      if (protocol.equals("jar")) {
        names ++= allClassesInJar(packageURL, packageNameSlashed)
      } else if (protocol.equals("file")) {
        names ++= allClassesInDirectory(packageName, new File(packageURL.getFile))
      } else {
        throw new IllegalArgumentException(s"Unsupported protocol $protocol in URL $packageURL for package $packageName.")
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
        addEntryIfClass(names, None, entryNameSlashed)
      }
    }

    names.toSet
  }

  private def allClassesInDirectory(packageName: String, dir: File): Set[ClassName] = {
    val names = mutable.HashSet[ClassName]()
    val content = dir.listFiles
    for (actual <- content) {
      val entryName = actual.getName
      val added = addEntryIfClass(names, Some(packageName), entryName)
      if (!added) {
        val entryFile = new File(dir, entryName)
        if (entryFile.isDirectory) {
          names ++= allClassesInDirectory(s"$packageName.$entryName", entryFile)
        } else {
          logger.debug(s"Not a class or a directory: $packageName for package $entryName")
        }
      }
    }

    names.toSet
  }

  /**
   * @return True if the entry was a class and hence was added.
   */
  private def addEntryIfClass(names: mutable.Set[ClassName], packageName: Option[String], entryNameSlashed: String) = {
    if (isClass(entryNameSlashed)) {
      val entryNameSlashedWithoutDotClass = entryNameSlashed.substring(0, entryNameSlashed.length - 6)
      val className = packageName.map(_ + '.').getOrElse("") + entryNameSlashedWithoutDotClass.replace('/', '.')
      names += ClassName(className)
      logger.debug(s"Adding class $className")
      true
    } else {
      false
    }
  }

  private def isClass(entryName: String) = entryName.endsWith(".class")
}
