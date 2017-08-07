package com.kenkoooo.sugoi

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.kenkoooo.sugoi.SugoiMapper.mapper
import org.scalatest.{FunSuite, Matchers}

import scala.collection.mutable.ArrayBuffer

class GameStateSuite extends FunSuite with Matchers {
  test("used edge check") {
    val url = getClass.getClassLoader.getResource("sample.json").toURI.toURL
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val map = mapper.readValue[LambdaMap](url, classOf[LambdaMap])
    val graph = new GameState(map, 10, new ArrayBuffer[Array[LambdaFuture]]())

    graph.isUsed(1, 3) shouldBe false
    graph.addEdge(1, 3, 2)
    graph.isUsed(1, 3) shouldBe true
  }

  test("state test") {
    val list =
      """ {"punter":0,"punters":2,"map":{"sites":[{"id":0,"x":0.0,"y":0.0},{"id":1,"x":1.0,"y":0.0},{"id":2,"x":2.0,"y":0.0},{"id":3,"x":2.0,"y":-1.0},{"id":4,"x":2.0,"y":-2.0},{"id":5,"x":1.0,"y":-2.0},{"id":6,"x":0.0,"y":-2.0},{"id":7,"x":0.0,"y":-1.0}],"rivers":[{"source":0,"target":1},{"source":1,"target":2},{"source":0,"target":7},{"source":7,"target":6},{"source":6,"target":5},{"source":5,"target":4},{"source":4,"target":3},{"source":3,"target":2},{"source":1,"target":7},{"source":1,"target":3},{"source":7,"target":5},{"source":5,"target":3}],"mines":[1,5]},"settings":{"futures":true,"splurges":true}}

 [{"claim":{"punter":0,"source":5,"target":7}},{"claim":{"punter":1,"source":3,"target":1}}]
 {"claim":{"target":1,"source":7,"punter":0}}
 [{"claim":{"punter":0,"source":7,"target":1}},{"claim":{"punter":1,"source":3,"target":5}}]
 {"claim":{"target":6,"source":7,"punter":0}}
 [{"claim":{"punter":0,"source":7,"target":6}},{"claim":{"punter":1,"source":1,"target":0}}]
 {"claim":{"source":5,"punter":0,"target":4}}
 [{"claim":{"punter":0,"source":5,"target":4}},{"claim":{"punter":1,"source":2,"target":1}}]
 {"claim":{"target":0,"punter":0,"source":7}}
 [{"claim":{"punter":0,"source":7,"target":0}},{"claim":{"punter":1,"source":3,"target":4}}]
 {"claim":{"source":2,"target":3,"punter":0}}
 {"stop":{"moves":[{"claim":{"punter":0,"source":2,"target":3}},{"claim":{"punter":1,"source":5,"target":6}}],"scores":[{"punter":0,"score":36},{"punter":1,"score":48}]}}
   """.stripMargin.split("\n")

    val setupToPunter = mapper.readValue[SetupToPunter](list(0), classOf[SetupToPunter])
    val gameState = new GameState(setupToPunter.map, 2, new ArrayBuffer[Array[LambdaFuture]]())
    for (i <- 2 until list.length - 2 if i % 2 == 0) {
      val move = mapper.readValue[List[ClaimMove]](list(i), new TypeReference[List[ClaimMove]] {})
      move.foreach(cm => {
        val s = cm.claim.source
        val t = cm.claim.target
        val p = cm.claim.punter
        gameState.addEdge(s, t, p)
      })

      val claimMove = mapper.readValue[ClaimMove](list(i + 1), classOf[ClaimMove])
      val s = claimMove.claim.source
      val t = claimMove.claim.target
      gameState.addEdge(s, t, 0)
    }
    gameState.addEdge(5, 6, 1)

    gameState.calcScore()(0) shouldBe 25
    gameState.calcScore()(1) shouldBe 30
  }

  test("option test") {
    val url = getClass.getClassLoader.getResource("sample.json").toURI.toURL
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val map = mapper.readValue[LambdaMap](url, classOf[LambdaMap])
    val graph = new GameState(map, 2, new ArrayBuffer[Array[LambdaFuture]]())

    graph.isUsed(0, 1) shouldBe false
    graph.canBuy(0, 1, 0) shouldBe false
    graph.addEdge(0, 1, 0)

    graph.isUsed(0, 1) shouldBe true
    graph.canBuy(0, 1, 0) shouldBe false
    graph.canBuy(0, 1, 1) shouldBe true
    graph.buyEdge(0, 1, 0)
    graph.canBuy(0, 1, 0) shouldBe false
  }

  test("option test2") {
    val url = getClass.getClassLoader.getResource("Sierpinski-triangle.json").toURI.toURL
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val map = mapper.readValue[LambdaMap](url, classOf[LambdaMap])
    val graph = new GameState(map, 3, new ArrayBuffer[Array[LambdaFuture]]())
    graph.addEdge(3, 13, 0)
    graph.addEdge(3, 12, 1)
    graph.addEdge(21, 3, 2)
  }
}
