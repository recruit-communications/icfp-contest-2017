package com.kenkoooo.yabai

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.collection.mutable

object YabaiSelector {
  type PunterId = String
  type MapId = String


  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def main(args: Array[String]): Unit = {
    val resultList = mapper.readValue[List[GameResult]](YabaiUrl.get(YabaiUrl.gameLog), new TypeReference[List[GameResult]] {})
    val zeroCount = new mutable.TreeMap[PunterId, Int]()
    resultList.foreach(r => {
      r.results.sorted
    })


  }

  case class GameResult(map: MapId,
                        results: Array[PlayerResult],
                        @JsonProperty("created_at") createdAt: Long,
                        id: String,
                        @JsonProperty("punter_ids") punterIds: Array[String])

  case class PlayerResult(score: Long, punter: PunterId)

}


object YabaiUrl {
  val gameLog = "http://13.112.208.142:3000/game/list"

  def get(url: String): String = scala.io.Source.fromURL(url).mkString
}

