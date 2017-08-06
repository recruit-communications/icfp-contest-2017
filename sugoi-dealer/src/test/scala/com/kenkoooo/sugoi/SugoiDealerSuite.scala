package com.kenkoooo.sugoi

import java.io.File

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSuite, Matchers}

class SugoiDealerSuite extends FunSuite with Matchers with MockitoSugar {
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

  test("pass test") {
    val program = mock[PunterProgram]
    val gameState = mock[GameState]

    when(program.punter).thenReturn(0)
    when(program.penaltyCount).thenReturn(0)
    when(program.putCommand(any(), any())).thenReturn(("{\"pass\":{\"punter\":0}}", 0L))
    when(gameState.edgeCount).thenReturn(1)
    SugoiDealer.play(Seq(program), gameState)
  }
}
