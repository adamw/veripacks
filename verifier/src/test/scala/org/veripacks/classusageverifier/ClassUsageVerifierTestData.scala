package org.veripacks.classusageverifier

import org.veripacks.{ClassName, Pkg}

trait ClassUsageVerifierTestData {
  val pkg1 = Pkg("foo.bar.p1")
  val pkg1_1 = Pkg("foo.bar.p1.pp1")
  val pkg1_1_1 = Pkg("foo.bar.p1.pp1.ppp1")
  val pkg1_1_2 = Pkg("foo.bar.p1.pp1.ppp2")

  val pkg2 = Pkg("foo.bar.p2")

  val cls1_in_pkg1 = ClassName(pkg1, "cls1_in_pkg1")
  val cls2_in_pkg1 = ClassName(pkg1, "cls2_in_pkg1")
  val cls3_in_pkg1 = ClassName(pkg1, "cls3_in_pkg1")

  val cls1_in_pkg1_1 = ClassName(pkg1_1, "cls1_in_pkg1_1")
  val cls2_in_pkg1_1 = ClassName(pkg1_1, "cls2_in_pkg1_1")

  val cls1_in_pkg1_1_1 = ClassName(pkg1_1_1, "cls1_in_pkg1_1_1")
  val cls2_in_pkg1_1_1 = ClassName(pkg1_1_1, "cls2_in_pkg1_1_1")

  val cls1_in_pkg1_1_2 = ClassName(pkg1_1_2, "cls2_in_pkg1_1_2")

  val cls1_in_pkg2 = ClassName(pkg2, "cls1_in_pkg2")
}
