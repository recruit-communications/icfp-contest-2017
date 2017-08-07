package com.kenkoooo.sugoi

import java.io.File

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSuite, Matchers}

class SugoiDealerSuite extends FunSuite with Matchers with MockitoSugar {
  test("fork process") {
    val testAi = new File(getClass.getClassLoader.getResource("test_ai.py").toURI).toPath.toString
    val punterProgram = new PunterProgram(s"python $testAi", 1, true, 10)
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

  test("option test") {
    val program = mock[PunterProgram]
    val gameState = mock[GameState]

    when(program.punter).thenReturn(1)
    when(program.penaltyCount).thenReturn(9)
    when(program.putCommand(any(), any())).thenReturn(("{\"option\":{\"punter\":1,\"source\":5,\"target\":7},\"state\":[\"test\"]}", 0L))
    when(program.optionRemain).thenReturn(1)

    when(gameState.edgeCount).thenReturn(1)
    when(gameState.canBuy(5, 7, 1)).thenReturn(true)

    val deque = SugoiDealer.play(Seq(program), gameState)
    deque(0) shouldBe a[OptionMove]
    deque(0).asInstanceOf[OptionMove].option.punter shouldBe 1
    deque(0).asInstanceOf[OptionMove].option.source shouldBe 5
    deque(0).asInstanceOf[OptionMove].option.target shouldBe 7

    verify(gameState, times(1)).buyEdge(5, 7, 1)
  }

  test("splurge option") {
    val program = mock[PunterProgram]
    val gameState = mock[GameState]

    when(program.punter).thenReturn(1)
    when(program.penaltyCount).thenReturn(9)
    when(program.putCommand(any(), any())).thenReturn(("{\"splurge\":{\"punter\":1,\"route\":[0, 1, 2, 3]},\"state\":[\"test\"]}", 0L))
    when(program.optionRemain).thenReturn(1)
    when(program.passCount).thenReturn(2)

    when(gameState.edgeCount).thenReturn(1)
    when(gameState.canBuy(0, 1, 1)).thenReturn(false)
    when(gameState.canBuy(1, 2, 1)).thenReturn(true)
    when(gameState.canBuy(2, 3, 1)).thenReturn(false)
    when(gameState.isUsed(0, 1)).thenReturn(false)
    when(gameState.isUsed(1, 2)).thenReturn(true)
    when(gameState.isUsed(2, 3)).thenReturn(false)

    val deque = SugoiDealer.play(Seq(program), gameState)
    deque(0) shouldBe a[SplurgeMove]
    deque(0).asInstanceOf[SplurgeMove].splurge.route.length shouldBe 4

    verify(gameState, times(1)).addEdge(0, 1, 1)
    verify(gameState, times(1)).buyEdge(1, 2, 1)
    verify(gameState, times(1)).addEdge(2, 3, 1)
  }

  test("failed splurge option (lack of option)") {
    val program = mock[PunterProgram]
    val gameState = mock[GameState]

    when(program.punter).thenReturn(1)
    when(program.penaltyCount).thenReturn(9)
    when(program.putCommand(any(), any())).thenReturn(("{\"splurge\":{\"punter\":1,\"route\":[0, 1, 2, 3]},\"state\":[\"test\"]}", 0L))
    when(program.optionRemain).thenReturn(0)
    when(program.passCount).thenReturn(2)

    when(gameState.edgeCount).thenReturn(1)
    when(gameState.canBuy(0, 1, 1)).thenReturn(false)
    when(gameState.canBuy(1, 2, 1)).thenReturn(true)
    when(gameState.canBuy(2, 3, 1)).thenReturn(false)
    when(gameState.isUsed(0, 1)).thenReturn(false)
    when(gameState.isUsed(1, 2)).thenReturn(true)
    when(gameState.isUsed(2, 3)).thenReturn(false)

    val deque = SugoiDealer.play(Seq(program), gameState)
    deque(0) shouldBe a[PassMove]

    verify(gameState, times(0)).addEdge(anyInt(), anyInt(), anyInt())
    verify(gameState, times(0)).buyEdge(anyInt(), anyInt(), anyInt())
  }

  test("splurge option (lack of pass)") {
    val program = mock[PunterProgram]
    val gameState = mock[GameState]

    when(program.punter).thenReturn(1)
    when(program.penaltyCount).thenReturn(9)
    when(program.putCommand(any(), any())).thenReturn(("{\"splurge\":{\"punter\":1,\"route\":[0, 1, 2, 3]},\"state\":[\"test\"]}", 0L))
    when(program.optionRemain).thenReturn(1)
    when(program.passCount).thenReturn(1)

    when(gameState.edgeCount).thenReturn(1)
    when(gameState.canBuy(0, 1, 1)).thenReturn(false)
    when(gameState.canBuy(1, 2, 1)).thenReturn(true)
    when(gameState.canBuy(2, 3, 1)).thenReturn(false)
    when(gameState.isUsed(0, 1)).thenReturn(false)
    when(gameState.isUsed(1, 2)).thenReturn(true)
    when(gameState.isUsed(2, 3)).thenReturn(false)

    val deque = SugoiDealer.play(Seq(program), gameState)
    deque(0) shouldBe a[PassMove]

    verify(gameState, times(0)).addEdge(anyInt(), anyInt(), anyInt())
    verify(gameState, times(0)).buyEdge(anyInt(), anyInt(), anyInt())
  }

  test("splurge option (can not buy)") {
    val program = mock[PunterProgram]
    val gameState = mock[GameState]

    when(program.punter).thenReturn(1)
    when(program.penaltyCount).thenReturn(9)
    when(program.putCommand(any(), any())).thenReturn(("{\"splurge\":{\"punter\":1,\"route\":[0, 1, 2, 3]},\"state\":[\"test\"]}", 0L))
    when(program.optionRemain).thenReturn(1)
    when(program.passCount).thenReturn(2)

    when(gameState.edgeCount).thenReturn(1)
    when(gameState.canBuy(0, 1, 1)).thenReturn(false)
    when(gameState.canBuy(1, 2, 1)).thenReturn(false)
    when(gameState.canBuy(2, 3, 1)).thenReturn(false)
    when(gameState.isUsed(0, 1)).thenReturn(false)
    when(gameState.isUsed(1, 2)).thenReturn(true)
    when(gameState.isUsed(2, 3)).thenReturn(false)

    val deque = SugoiDealer.play(Seq(program), gameState)
    deque(0) shouldBe a[PassMove]

    verify(gameState, times(0)).addEdge(anyInt(), anyInt(), anyInt())
    verify(gameState, times(0)).buyEdge(anyInt(), anyInt(), anyInt())
  }
}
