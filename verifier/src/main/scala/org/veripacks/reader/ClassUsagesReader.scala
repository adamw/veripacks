package org.veripacks.reader

import org.veripacks.{ClassName, ClassUsage}
import javassist.ClassPool
import com.typesafe.scalalogging.slf4j.Logging

class ClassUsagesReader extends Logging {
  def read(className: ClassName): List[ClassUsage] = {
    Nil
  }
}
