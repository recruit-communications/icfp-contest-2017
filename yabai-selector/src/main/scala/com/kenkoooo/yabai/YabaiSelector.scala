package com.kenkoooo.yabai

import java.time.Instant

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

  val AFTER_TIME_UTC_STRING = "2017-08-07T03:30:00Z"

  def main(args: Array[String]): Unit = {
    val maxSize = args(0).toInt * 16

    val punterEntries = mapper.readValue[List[PunterEntry]](YabaiUrl.get(YabaiUrl.punterList), new TypeReference[List[PunterEntry]] {})
    val mapEntries = mapper.readValue[List[MapEntry]](YabaiUrl.get(YabaiUrl.mapList), new TypeReference[List[MapEntry]] {})
    val gameList = mapper.readValue[List[GameResult]](YabaiUrl.get(YabaiUrl.gameLog), new TypeReference[List[GameResult]] {})

    val queue = new ArrayBuffer[Execute]()

    def extractAndListUp(member: Int): List[Data] = {
      val mapIds = (for (map <- mapEntries if map.punterNum == member) yield map.id).toSet

      val punterCount = new mutable.TreeMap[PunterId, Int]().withDefaultValue(0)
      val punterScore = new mutable.TreeMap[PunterId, Double]().withDefaultValue(0.0)

      gameList.foreach(gameResult => {
        val mapId = gameResult.map
        if (gameResult.createdAtMillis > Instant.parse(AFTER_TIME_UTC_STRING).getEpochSecond * 1000 && mapIds.contains(mapId))
          Option(gameResult.results).foreach { results =>
            val member = results.length
            var rank: Int = 0
            if (member > 1)
              results.zipWithIndex.foreach { case (result, i) =>
                val punterId = result.punter
                if (i > 0 && results(rank).score != results(i).score) rank = i
                val point = 1.0 - rank.toDouble / (member.toDouble - 1.0)

                punterScore(punterId) += point
                punterCount(punterId) += 1
              }
          }
      })

      val x = for (entry <- punterEntries if punterCount(entry.id) > 0) yield (entry.id, punterScore(entry.id) / punterCount(entry.id), punterCount(entry.id))
      for ((punterId, score, count) <- x.sortBy { case (_, score, _) => score }.reverse) yield Data(punterId, score, count)
    }

    def select(data: List[Data], member: Int): Unit = {
      val mapIds = (for (map <- mapEntries if map.punterNum == member) yield map.id).toSet

      val punterCount = new mutable.TreeMap[PunterId, Int]().withDefaultValue(0)
      val punterScore = new mutable.TreeMap[PunterId, Double]().withDefaultValue(0.0)

      data.foreach(d => {
        punterCount(d.punterId) = d.count
        punterScore(d.punterId) = d.score
      })

      punterEntries.foreach(entry => {
        if (!punterScore.contains(entry.id) || punterCount(entry.id) < 10) {
          punterScore(entry.id) = 1.0
        }
      })

      val selectedPunters = punterScore.toList.sortBy { case (_, score) => score }.reverse.take(16)
      for (_ <- 0 until (maxSize / member)) {
        val shuffled = Random.shuffle(selectedPunters)
        queue.append(Execute(Random.shuffle(mapIds.toList).head, (for (i <- 0 until member) yield shuffled(i % shuffled.length)._1).toList))
      }
    }

    (for (map <- mapEntries) yield map.punterNum).distinct.sorted.foreach(member => {
      val data = extractAndListUp(member)
      logger.info(s"member: $member")
      data.foreach(d => logger.info(mapper.writeValueAsString(d)))
      logger.info("")
      select(data, member)
    })

    Random.shuffle(queue).take(args(0).toInt).foreach(q => YabaiUrl.get(YabaiUrl.gameExecute(q.mapId, q.punters)))
  }

  case class Execute(mapId: LambdaMapId, punters: List[PunterId])

  case class Data(punterId: PunterId, score: Double, count: Int)

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class GameResult(@JsonProperty("map_id") map: LambdaMapId,
                        results: Array[PlayerResult],
                        @JsonProperty("created_at") createdAtMillis: Long,
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
  val gameLog = s"$host/game/list?count=5000&all=1"
  val punterList = s"$host/punter/list"
  val mapList = s"$host/map/list"

  def gameExecute(mapId: String, punterIds: List[String]): String = s"$host/game/execute?map_id=$mapId&punter_ids=${Random.shuffle(punterIds).mkString(",")}"

  def get(url: String): String = {
    logger.info(s"GET $url")
    scala.io.Source.fromURL(url).mkString
  }
}

