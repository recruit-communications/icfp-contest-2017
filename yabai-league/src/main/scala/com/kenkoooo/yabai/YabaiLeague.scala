package com.kenkoooo.yabai

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.logging.log4j.scala.Logging

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

    gameList.foreach(gameResult => {
      Option(gameResult.results) match {
        case Some(results) => {}
        case _ => {

        }
      }
    })
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

