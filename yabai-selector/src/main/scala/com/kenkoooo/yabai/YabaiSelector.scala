package com.kenkoooo.yabai

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.collection.mutable

object YabaiSelector {
  type PunterId = String
  type LambdaMapId = String

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  val ILLEGAL_ZERO_SLESHHOLD = 5
  val FEWER_SELECTED_MAP_TOP = 5

  def main(args: Array[String]): Unit = {
    val resultList = mapper.readValue[List[GameResult]](YabaiUrl.get(YabaiUrl.gameLog), new TypeReference[List[GameResult]] {})
    val zeroCount = new mutable.TreeMap[PunterId, Int]().withDefaultValue(0)
    val mapSelected = new mutable.TreeMap[LambdaMapId, Int]().withDefaultValue(0)
    val mapMemberCount = new mutable.TreeMap[LambdaMapId, Int]().withDefaultValue(0)
    val punterIdCount = new mutable.TreeMap[PunterId, Int]().withDefaultValue(0)
    resultList.foreach(r => Option(r.results).foreach(_.foreach(g => {
      if (g.score == 0) zeroCount(g.punter) += 1
      else punterIdCount(g.punter) += 1
      mapSelected(r.map) += 1
      if (mapMemberCount(r.map) < r.results.length) mapMemberCount(r.map) = r.results.length
    })))

    mapSelected.foreach { case (mapId, count) => mapSelected(mapId) = count / mapMemberCount(mapId) }

    val nullAiSet = new mutable.TreeSet[String]()
    for((punterId,_)<-  zeroCount)yield punterId

    zeroCount.foreach { case (punterId, count) => if (count >= ILLEGAL_ZERO_SLESHHOLD) nullAiSet += punterId }
    val fewerSelectedMapIds = for ((mapId, _) <- mapSelected.toArray.sortBy { case (_, count) => count }.toList.slice(0, FEWER_SELECTED_MAP_TOP)) yield mapId
    println(fewerSelectedMapIds)

  }

  case class GameResult(map: LambdaMapId,
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

