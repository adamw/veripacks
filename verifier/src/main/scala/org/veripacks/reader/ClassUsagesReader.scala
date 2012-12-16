package org.veripacks.reader

import org.veripacks.{Pkg, ClassName, ClassUsage}
import javassist.ClassPool
import com.typesafe.scalalogging.slf4j.Logging

class ClassUsagesReader extends Logging {
  def read(usagesIn: ClassName, scope: Pkg): List[ClassUsage] = {
    Nil
  }
}
