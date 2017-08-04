package com.kenkoooo.sugoi

import org.scalatest.{FunSuite, Matchers}

class SugoiDealerSuite extends FunSuite with Matchers {
  test("run the program successfully") {
    new AiProgram("cat").put("a", 10) shouldBe("a", 0)
  }
}
