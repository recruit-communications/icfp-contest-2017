package com.kenkoooo.yabai

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.logging.log4j.scala.Logging

import scala.collection.mutable
import scala.util.Random

object YabaiSelector extends Logging {
  type PunterId = String
  type LambdaMapId = String

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def main(args: Array[String]): Unit = {
    val punterIds = mapper.readValue[List[PunterEntry]](YabaiUrl.get(YabaiUrl.punterList), new TypeReference[List[PunterEntry]] {})
    val maps = mapper.readValue[List[MapEntry]](YabaiUrl.get(YabaiUrl.mapList), new TypeReference[List[MapEntry]] {})
    val gameList = mapper.readValue[List[GameResult]](YabaiUrl.get(YabaiUrl.gameLog), new TypeReference[List[GameResult]] {})

    val mapIds = (for (map <- maps) yield map.id).toSet
    val mapMember = new mutable.TreeMap[PunterId, Int]()
    maps.foreach(map => mapMember += (map.id -> map.punterNum))

    val punterCount = new mutable.TreeMap[PunterId, Int]().withDefaultValue(0)
    val punterScore = new mutable.TreeMap[PunterId, Double]().withDefaultValue(0.0)

    gameList.foreach(gameResult => {
      val mapId = gameResult.map
      if (mapIds.contains(mapId)) Option(gameResult.results).foreach { results =>
        val member = results.length
        if (member > 1) results.zipWithIndex.foreach { case (result, i) =>
          val punterId = result.punter
          val point = 1.0 - i.toDouble / (member.toDouble - 1.0)

          punterScore(punterId) += point
          punterCount(punterId) += 1
        }
      }
    })

    val x = for (entry <- punterIds if punterCount(entry.id) > 0) yield (entry.id, punterScore(entry.id) / punterCount(entry.id), punterCount(entry.id))
    x.sortBy { case (_, score, _) => score }.reverse.foreach {
      case (punterId, score, count) =>
        logger.info(s"${mapper.writeValueAsString(Data(punterId, score, count))}")
    }
  }

  case class Data(punterId: PunterId, score: Double, count: Int)

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
  case class MapEntry(info: MapInfo, @JsonProperty("created_at") createdAt: Long, id: LambdaMapId, @JsonProperty("punter_num") punterNum: Int, url: String)

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

