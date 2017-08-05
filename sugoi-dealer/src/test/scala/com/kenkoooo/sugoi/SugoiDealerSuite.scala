package com.kenkoooo.sugoi

import java.io.File

import org.scalatest.{FunSuite, Matchers}

class SugoiDealerSuite extends FunSuite with Matchers {
  test("fork process") {
    val testAi = new File(getClass.getClassLoader.getResource("test_ai.py").toURI).toPath.toString
    val punterProgram = new PunterProgram(s"python $testAi", 1)
    punterProgram.putCommand("", 10) shouldBe("123456789\n", 0)
  }

}
