package com.kenkoooo.yabai

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.logging.log4j.scala.Logging

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object YabaiSelector extends Logging {
  type PunterId = String
  type LambdaMapId = String

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  val ILLEGAL_PUNTER_RATIO = 0.5
  val ILLEGAL_MAP_COUNT = 10
  val FEWER_SELECTED_MAP_TOP = 10
  val PARALLEL_BATTLE_COUNT = 5

  def main(args: Array[String]): Unit = {
    val zeroCount = new mutable.TreeMap[PunterId, Int]().withDefaultValue(0)
    val mapSelected = new mutable.TreeMap[LambdaMapId, Int]().withDefaultValue(0)
    val mapMemberCount = new mutable.TreeMap[LambdaMapId, Int]().withDefaultValue(0)
    val punterIdCount = new mutable.TreeMap[PunterId, Int]().withDefaultValue(0)
    val illegalMapCount = new mutable.TreeMap[LambdaMapId, Int]().withDefaultValue(0)

    val validPunterIds = (for (entry <- mapper.readValue[List[PunterEntry]](YabaiUrl.get(YabaiUrl.punterList), new TypeReference[List[PunterEntry]] {})) yield entry.id).toSet
    validPunterIds.foreach(punterId => punterIdCount(punterId) = 0)

    mapper.readValue[List[MapEntry]](YabaiUrl.get(YabaiUrl.mapList), new TypeReference[List[MapEntry]] {}).foreach(entry => {
      mapMemberCount(entry.id) = entry.punterNum
      mapSelected(entry.id) = 0
    })
    mapper.readValue[List[GameResult]](YabaiUrl.get(YabaiUrl.gameLog), new TypeReference[List[GameResult]] {})
      .foreach(r => Option(r.results) match {
        case Some(result) => result.foreach(g =>
          if (validPunterIds.contains(g.punter)) {
            if (g.score == 0) zeroCount(g.punter) += 1
            punterIdCount(g.punter) += 1
            mapSelected(r.map) += 1
          })
        case _ =>
          illegalMapCount(r.map) += 1
      }

      )

    mapSelected.foreach { case (mapId, count) => mapSelected(mapId) = count / mapMemberCount(mapId) }
    zeroCount.foreach { case (punterId, count) =>
      if (punterIdCount(punterId) > 10 && count.toDouble / punterIdCount(punterId).toDouble > ILLEGAL_PUNTER_RATIO) {
        logger.info(s"zero point ratio: $punterId: $count / ${punterIdCount(punterId)} = ${count.toDouble / punterIdCount(punterId).toDouble}")
        punterIdCount.remove(punterId)
      }
    }

    illegalMapCount.foreach { case (mapId, count) =>
      if (count > ILLEGAL_MAP_COUNT && mapSelected.contains(mapId)) {
        mapSelected.remove(mapId)
        logger.info(s"illegal count $mapId: $count")
      }
    }

    val sortedPunters = for ((punterId, _) <- punterIdCount.toArray.sortBy { case (_, count) => count } if validPunterIds.contains(punterId)) yield punterId
    var pos = 0
    Random.shuffle(mapSelected.toArray.sortBy { case (_, count) => count }.toList.take(FEWER_SELECTED_MAP_TOP))
      .take(PARALLEL_BATTLE_COUNT)
      .foreach { case (mapId, _) =>
        val punterIds = new ArrayBuffer[PunterId]()
        for (_ <- 0 until mapMemberCount(mapId)) {
          punterIds.append(sortedPunters(pos % sortedPunters.length))
          pos += 1
        }
        YabaiUrl.get(YabaiUrl.gameExecute(mapId, punterIds.toList))
      }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class GameResult(@JsonProperty("map_id") map: LambdaMapId,
                        results: Array[PlayerResult],
                        @JsonProperty("created_at") createdAt: Long,
                        id: String,
                        @JsonProperty("punter_ids") punterIds: Array[String],
                        job: Job)

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class PlayerResult(score: Long, punter: PunterId)

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class PunterEntry(id: PunterId, @JsonProperty("created_at") createdAt: Long)

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class Job(url: String, status: String)

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class MapEntry(info: MapInfo, @JsonProperty("created_at") createdAt: Long, id: String, @JsonProperty("punter_num") punterNum: Int, url: String)

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class MapInfo(rivers: Long, mines: Int, sites: Int)

}


object YabaiUrl extends Logging {
  val host = "http://13.112.208.142:3000"
  val gameLog = s"$host/game/list"
  val punterList = s"$host/punter/list"
  val mapList = s"$host/map/list"

  def gameExecute(mapId: String, punterIds: List[String]): String = s"$host/game/execute?map_id=$mapId&punter_ids=${Random.shuffle(punterIds).mkString(",")}"

  def get(url: String): String = {
    logger.info(s"GET $url")
    scala.io.Source.fromURL(url).mkString
  }
}

