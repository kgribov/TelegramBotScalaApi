package com.kgribov.telegram.source

import org.scalatest.{FunSuite, Matchers}

class OffsetStoreTest extends FunSuite with Matchers {

  test("should load offset from file") {
    val offsetStore = new FileBasedOffsetStore("", "src/test/resources")
    offsetStore.loadOffset should be (111)
  }

  test("should return zero offset if file not exist") {
    val offsetStore = new FileBasedOffsetStore("", "hahah")
    offsetStore.loadOffset should be (0)
  }
}
