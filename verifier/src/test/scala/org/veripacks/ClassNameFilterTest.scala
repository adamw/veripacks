package org.veripacks

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.veripacks.ClassNameFilter._

class ClassNameFilterTest extends FlatSpec with ShouldMatchers {
  val simpleFilter = AllUnknownClassNameFilter
    .or(IncludeClassNameFilter(List("org")))
    .or(ExcludeClassNameFilter(List("com.foo")))
    .or(IncludeClassNameFilter(List("com.bar")))

  val catchAllFilter = AllUnknownClassNameFilter
    .or(ExcludeClassNameFilter(List("com.foo")))
    .or(IncludeClassNameFilter(List("")))

  val multiPackagesFilter = AllUnknownClassNameFilter
    .or(ExcludeClassNameFilter(List("com.foo", "com.bar")))
    .or(IncludeClassNameFilter(List("com")))

  it should "properly filter using the simple filter" in {
    simpleFilter.includes(ClassName(Pkg("org.apache"), "Http")) should be (Yes)
    simpleFilter.includes(ClassName(Pkg("com.foo.baz"), "Service")) should be (No)
    simpleFilter.includes(ClassName(Pkg("com.bar.baz"), "Service")) should be (Yes)
    simpleFilter.includes(ClassName(Pkg("net.lib"), "Http")) should be (Unknown)
  }

  it should "properly filter using the catch all filter" in {
    catchAllFilter.includes(ClassName(Pkg("com.foo.baz"), "Service")) should be (No)
    catchAllFilter.includes(ClassName(Pkg("com.bar.baz"), "Service")) should be (Yes)
    catchAllFilter.includes(ClassName(Pkg("org"), "Service")) should be (Yes)
  }

  it should "properly filter using the multi packages filter" in {
    multiPackagesFilter.includes(ClassName(Pkg("com.foo.baz"), "Service")) should be (No)
    multiPackagesFilter.includes(ClassName(Pkg("com.bar.baz"), "Service")) should be (No)
    multiPackagesFilter.includes(ClassName(Pkg("com.baz"), "Service")) should be (Yes)
  }
}
