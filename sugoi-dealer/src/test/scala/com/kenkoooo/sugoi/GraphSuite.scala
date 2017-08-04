package com.kenkoooo.sugoi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.scalatest.{FunSuite, Matchers}

class GraphSuite extends FunSuite with Matchers {
  test("used edge check") {
    val url = getClass.getClassLoader.getResource("sample.json").toURI.toURL
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val map = mapper.readValue[LambdaMap](url, classOf[LambdaMap])
    val graph = new Graph(map)

    graph.isUsed(1, 3) shouldBe false
    graph.addEdge(1, 3, 2)
    graph.isUsed(1, 3) shouldBe true
    graph.addEdge(0, 1, 2)
    graph.remainEdgeCount shouldBe 10
    graph.addEdge(1, 2, 2)
    graph.remainEdgeCount shouldBe 9
    graph.addEdge(0, 7, 2)
    graph.remainEdgeCount shouldBe 8
    graph.addEdge(7, 6, 2)
    graph.remainEdgeCount shouldBe 7
    graph.addEdge(6, 5, 2)
    graph.remainEdgeCount shouldBe 6
    graph.addEdge(5, 4, 2)
    graph.remainEdgeCount shouldBe 5
    graph.addEdge(4, 3, 2)
    graph.remainEdgeCount shouldBe 4
    graph.addEdge(3, 2, 2)
    graph.remainEdgeCount shouldBe 3
    graph.addEdge(1, 7, 2)
    graph.remainEdgeCount shouldBe 2
    graph.addEdge(7, 5, 2)
    graph.remainEdgeCount shouldBe 1
    graph.addEdge(3, 5, 2)
    graph.remainEdgeCount shouldBe 0
  }

}
