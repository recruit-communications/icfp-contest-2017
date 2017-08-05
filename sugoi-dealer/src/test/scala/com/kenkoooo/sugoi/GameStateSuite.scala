package com.kenkoooo.sugoi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.scalatest.{FunSuite, Matchers}

class GameStateSuite extends FunSuite with Matchers {
  test("used edge check") {
    val url = getClass.getClassLoader.getResource("sample.json").toURI.toURL
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val map = mapper.readValue[LambdaMap](url, classOf[LambdaMap])
    val graph = new GameState(map, 10)

    graph.isUsed(1, 3) shouldBe false
    graph.addEdge(1, 3, 2)
    graph.isUsed(1, 3) shouldBe true
  }

}
