package org.veripacks.data.t5.p52

import org.veripacks.data.t5.p51.{Class512, Class511}
import org.veripacks.NotVerified

@NotVerified
class Class521 {
  val i1 = new Class511 // ok
  val i2 = new Class512 // would be illegal
}
