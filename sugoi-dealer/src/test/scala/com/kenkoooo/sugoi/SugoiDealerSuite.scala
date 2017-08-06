package com.kenkoooo.sugoi

import java.io.File

import org.scalatest.{FunSuite, Matchers}

class SugoiDealerSuite extends FunSuite with Matchers {
  test("fork process") {
    val testAi = new File(getClass.getClassLoader.getResource("test_ai.py").toURI).toPath.toString
    val punterProgram = new PunterProgram(s"python $testAi", 1)
    val (_, code) = punterProgram.putCommand("{}", 10)
    code shouldBe 0
  }

  test("purify") {
    val json = "{\"move\":{\"moves\":[{\"pass\":{\"punter\":0}}]},\"state\":[\"1 0 1 8 12 2 1 2 1 -1 7 -1 4 0 -1 2 -1 3 -1 7 -1 2 1 -1 3 -1 4 1 -1 2 -1 4 -1 5 -1 2 3 -1 5 -1 4 3 -1 4 -1 6 -1 7 -1 2 5 -1 7 -1 4 0 -1 1 -1 5 -1 6 -1 1 5\",[0,1,2,3,4,5,6,7]]}"
    SugoiMapper.purify(json) shouldBe "{\"move\":{\"moves\":[{\"pass\":{\"punter\":0}}]}}"
  }
}
