package com.kenkoooo.sugoi

import org.scalatest.{FunSuite, Matchers}

class SugoiDealerSuite extends FunSuite with Matchers {
  test("run the program successfully") {
    new PunterProgram("./a.out", 1).putCommand("{}", 10) shouldBe("a", 0)
  }
}
