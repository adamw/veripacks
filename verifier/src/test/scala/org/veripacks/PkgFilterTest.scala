package org.veripacks

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.veripacks.PkgFilter._

class PkgFilterTest extends FlatSpec with ShouldMatchers {
  val simpleFilter = AllUnknownPkgFilter
    .or(IncludePkgFilter(List("org")))
    .or(ExcludePkgFilter(List("com.foo")))
    .or(IncludePkgFilter(List("com.bar")))

  val catchAllFilter = AllUnknownPkgFilter
    .or(ExcludePkgFilter(List("com.foo")))
    .or(IncludePkgFilter(List("")))

  val multiPackagesFilter = AllUnknownPkgFilter
    .or(ExcludePkgFilter(List("com.foo", "com.bar")))
    .or(IncludePkgFilter(List("com")))

  it should "properly filter using the simple filter" in {
    simpleFilter.includes(Pkg("org.apache")) should be (Yes)
    simpleFilter.includes(Pkg("com.foo.baz")) should be (No)
    simpleFilter.includes(Pkg("com.bar.baz")) should be (Yes)
    simpleFilter.includes(Pkg("net.lib")) should be (Unknown)
  }

  it should "properly filter using the catch all filter" in {
    catchAllFilter.includes(Pkg("com.foo.baz")) should be (No)
    catchAllFilter.includes(Pkg("com.bar.baz")) should be (Yes)
    catchAllFilter.includes(Pkg("org")) should be (Yes)
  }

  it should "properly filter using the multi packages filter" in {
    multiPackagesFilter.includes(Pkg("com.foo.baz")) should be (No)
    multiPackagesFilter.includes(Pkg("com.bar.baz")) should be (No)
    multiPackagesFilter.includes(Pkg("com.baz")) should be (Yes)
  }
}
