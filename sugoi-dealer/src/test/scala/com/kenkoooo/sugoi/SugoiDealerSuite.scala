package com.kenkoooo.sugoi

import org.scalatest.{FunSuite, Matchers}

class SugoiDealerSuite extends FunSuite with Matchers {
  test("run the program successfully") {
    new AiProgram(Array("cat"), 10).put("a") shouldBe("a", 0)
  }

  test("timeout execution") {
    val (out, code) = new AiProgram(Array("sleep", "10"), 2).put("")
    out shouldBe ""
    assert(code != 0)
  }

  test("failed execution") {
    val (out, code) = new AiProgram(Array("expr", "1", "/", "0"), 2).put("")
    out shouldBe ""
    assert(code != 0)
  }
}
