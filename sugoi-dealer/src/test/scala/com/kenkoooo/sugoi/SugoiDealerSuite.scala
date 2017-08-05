package com.kenkoooo.sugoi

import java.io.File

import org.scalatest.{FunSuite, Matchers}

class SugoiDealerSuite extends FunSuite with Matchers {
  test("fork process") {
    val testAi = new File(getClass.getClassLoader.getResource("test_ai.py").toURI).toPath.toString
    val punterProgram = new PunterProgram(s"python $testAi", 1)
    val (_, code) = punterProgram.putCommand("", 10)
    code shouldBe 0
  }
}
