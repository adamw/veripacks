package org.veripacks.data.dependenciesreader

class UsageInDefBody {
  private def m3() {
    val x: Cls3 = Cls3Builder.build

    x.toString

    Cls2Builder.build.toString
  }
}
