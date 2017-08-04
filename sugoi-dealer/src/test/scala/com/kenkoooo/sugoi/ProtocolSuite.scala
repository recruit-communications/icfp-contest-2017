package com.kenkoooo.sugoi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.scalatest.{FunSuite, Matchers}

class ProtocolSuite extends FunSuite with Matchers {
  test("run the program successfully") {
    val url = getClass.getClassLoader.getResource("sample.json").toURI.toURL
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val map = mapper.readValue[Map](url, classOf[Map])

    map.sites.length shouldBe 8
    map.rivers.length shouldBe 12
    map.mines.length shouldBe 2
  }
}
