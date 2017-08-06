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

    when(program.punter).thenReturn(1)
    when(program.penaltyCount).thenReturn(9)
    when(program.putCommand(any(), any())).thenReturn(("{\"pass\":{\"punter\":1}}", 0L))
    when(gameState.edgeCount).thenReturn(1)
    val deque = SugoiDealer.play(Seq(program), gameState)
    deque(0) shouldBe a[PassMove]
    deque(0).asInstanceOf[PassMove].pass.punter shouldBe 1
  }

  test("move test") {
    val program = mock[PunterProgram]
    val gameState = mock[GameState]

    when(program.punter).thenReturn(1)
    when(program.penaltyCount).thenReturn(9)
    when(program.putCommand(any(), any())).thenReturn(("{\"claim\":{\"punter\":1,\"source\":5,\"target\":7},\"state\":[\"test\"]}", 0L))
    when(gameState.edgeCount).thenReturn(1)
    val deque = SugoiDealer.play(Seq(program), gameState)
    deque(0) shouldBe a[ClaimMove]
    deque(0).asInstanceOf[ClaimMove].claim.punter shouldBe 1
    deque(0).asInstanceOf[ClaimMove].claim.source shouldBe 5
    deque(0).asInstanceOf[ClaimMove].claim.target shouldBe 7

    verify(gameState, times(1)).addEdge(5, 7, 1)
  }

  test("used edge move test") {
    val program = mock[PunterProgram]
    val gameState = mock[GameState]

    when(program.punter).thenReturn(1)
    when(program.penaltyCount).thenReturn(9)
    when(program.putCommand(any(), any())).thenReturn(("{\"claim\":{\"punter\":1,\"source\":5,\"target\":7},\"state\":[\"test\"]}", 0L))
    when(gameState.edgeCount).thenReturn(1)
    when(gameState.isUsed(5, 7)).thenReturn(true)
    val deque = SugoiDealer.play(Seq(program), gameState)
    deque(0) shouldBe a[PassMove]
    deque(0).asInstanceOf[PassMove].pass.punter shouldBe 1

    verify(gameState, times(0)).addEdge(anyInt(), anyInt(), anyInt())
  }

  test("splurge test") {
    val program = mock[PunterProgram]
    val gameState = mock[GameState]

    when(program.punter).thenReturn(1)
    when(program.penaltyCount).thenReturn(9)
    when(program.putCommand(any(), any())).thenReturn(("{\"claim\":{\"punter\":1,\"source\":5,\"target\":7},\"state\":[\"test\"]}", 0L))
    when(gameState.edgeCount).thenReturn(1)
    val deque = SugoiDealer.play(Seq(program), gameState)
    deque(0) shouldBe a[ClaimMove]
    deque(0).asInstanceOf[ClaimMove].claim.punter shouldBe 1
    deque(0).asInstanceOf[ClaimMove].claim.source shouldBe 5
    deque(0).asInstanceOf[ClaimMove].claim.target shouldBe 7

    verify(gameState, times(1)).addEdge(5, 7, 1)
  }
}
