package org.veripacks

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.veripacks.ClassUsageFilter._

class ClassUsageFilterTest extends FlatSpec with ShouldMatchers {
  val simpleFilter = AllUnknownClassUsageFilter
    .or(IncludeClassUsageFilter(List("org")))
    .or(ExcludeClassUsageFilter(List("com.foo")))
    .or(IncludeClassUsageFilter(List("com.bar")))

  val catchAllFilter = AllUnknownClassUsageFilter
    .or(ExcludeClassUsageFilter(List("com.foo")))
    .or(IncludeClassUsageFilter(List("")))

  val multiPackagesFilter = AllUnknownClassUsageFilter
    .or(ExcludeClassUsageFilter(List("com.foo", "com.bar")))
    .or(IncludeClassUsageFilter(List("com")))

  it should "properly filter using the simple filter" in {
    simpleFilter.shouldBeVerified(classUsageForPkg("org.apache.Http")) should be (Yes)
    simpleFilter.shouldBeVerified(classUsageForPkg("com.foo.baz.Service")) should be (No)
    simpleFilter.shouldBeVerified(classUsageForPkg("com.bar.baz.Service")) should be (Yes)
    simpleFilter.shouldBeVerified(classUsageForPkg("net.lib.Http")) should be (Unknown)
  }

  it should "properly filter using the catch all filter" in {
    catchAllFilter.shouldBeVerified(classUsageForPkg("com.foo.baz.Service")) should be (No)
    catchAllFilter.shouldBeVerified(classUsageForPkg("com.bar.baz.Service")) should be (Yes)
    catchAllFilter.shouldBeVerified(classUsageForPkg("org.Service")) should be (Yes)
  }

  it should "properly filter using the multi packages filter" in {
    multiPackagesFilter.shouldBeVerified(classUsageForPkg("com.foo.baz.Service")) should be (No)
    multiPackagesFilter.shouldBeVerified(classUsageForPkg("com.bar.baz.Service")) should be (No)
    multiPackagesFilter.shouldBeVerified(classUsageForPkg("com.baz.Service")) should be (Yes)
  }

  def classUsageForPkg(pkg: String) = ClassUsage(ClassName(Pkg(pkg), null), null, null)
}
