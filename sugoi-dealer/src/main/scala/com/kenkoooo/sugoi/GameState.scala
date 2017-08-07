package com.kenkoooo.sugoi


import java.util

import org.apache.logging.log4j.scala.Logging

import scala.collection.mutable.ArrayBuffer
import scala.collection.{immutable, mutable}

object GameState {
  val UNUSED: Int = -1
}

class GameState(map: LambdaMap, punterNum: Int, futures: ArrayBuffer[Array[LambdaFuture]]) extends Logging {
  type Punter = Int
  type Vertex = Int
  type Score = Long

  val futureVector: immutable.IndexedSeq[mutable.TreeMap[Vertex, Vertex]] = for (_ <- 0 until punterNum) yield new mutable.TreeMap[Vertex, Vertex]()
  futures.zipWithIndex.foreach(v => {
    val (arr, i) = v
    Option(arr).foreach(_.foreach(f => futureVector(i) += (f.source -> f.target)))
  })

  val usedGraph = new mutable.TreeMap[Vertex, mutable.TreeMap[Vertex, Punter]]
  val optionGraph = new mutable.TreeMap[Vertex, mutable.TreeMap[Vertex, Punter]]
  map.sites.foreach(site => {
    usedGraph += (site.id -> new mutable.TreeMap[Vertex, Punter]())
    optionGraph += (site.id -> new mutable.TreeMap[Vertex, Punter]())
  })
  map.rivers.foreach(river => {
    usedGraph(river.source) += (river.target -> GameState.UNUSED)
    usedGraph(river.target) += (river.source -> GameState.UNUSED)
    optionGraph(river.source) += (river.target -> GameState.UNUSED)
    optionGraph(river.target) += (river.source -> GameState.UNUSED)
  })
  val mines: Set[Vertex] = map.mines.toSet

  var edgeCount: Int = map.rivers.length

  def isUsed(source: Vertex, target: Vertex): Boolean = {
    !(usedGraph.contains(source) && usedGraph(source).contains(target) && usedGraph(source)(target) == GameState.UNUSED)
  }

  def canBuy(source: Vertex, target: Vertex, punter: Punter): Boolean = {
    optionGraph.contains(source) && optionGraph(source).contains(target) &&
      optionGraph(source)(target) == GameState.UNUSED &&
      usedGraph(source)(target) != GameState.UNUSED &&
      usedGraph(source)(target) != punter
  }

  /**
    * use edge
    *
    * @param source left side of the edge
    * @param target right side of the edge
    * @param punter user of this edge
    */
  def addEdge(source: Vertex, target: Vertex, punter: Punter): Unit = {
    usedGraph(source)(target) = punter
    usedGraph(target)(source) = punter
  }

  def buyEdge(source: Vertex, target: Vertex, punter: Punter): Unit = {
    optionGraph(source)(target) = punter
    optionGraph(target)(source) = punter
  }

  /**
    * calculate scores for all players
    *
    * @return Map[Player, Score]
    */
  def calcScore(): mutable.TreeMap[Punter, Score] = {
    val distFromMines: mutable.TreeMap[Vertex, mutable.TreeMap[Vertex, Long]] = {
      val res = new mutable.TreeMap[Vertex, mutable.TreeMap[Vertex, Long]]()

      mines.foreach(start => {
        val deque = new util.ArrayDeque[Vertex]()
        val dist = new mutable.TreeMap[Vertex, Long]
        dist += (start -> 0)
        deque.add(start)
        while (!deque.isEmpty) {
          val v = deque.poll()
          for ((u, _) <- usedGraph(v)) {
            if (!dist.contains(u)) {
              dist += (u -> (dist(v) + 1))
              deque.add(u)
            }
          }
        }

        res += (start -> dist)
      })
      res
    }

    val map = new mutable.TreeMap[Punter, Score]()
    for (punter <- 0 until punterNum) map += (punter -> calcForOne(punter, distFromMines))
    map
  }

  private def calcForOne(punter: Punter, distFromMines: mutable.TreeMap[Vertex, mutable.TreeMap[Vertex, Long]]): Score = {
    val sourceToTarget = futureVector(punter)

    var score: Score = 0
    mines.foreach(start => {
      // naive BFS
      val dist = new mutable.TreeMap[Vertex, Int]()
      dist += (start -> 0)
      val deque = new util.ArrayDeque[Vertex]()
      deque.add(start)
      while (!deque.isEmpty) {
        val v = deque.poll()
        for ((u, p) <- usedGraph(v)) if ((p == punter || optionGraph(v)(u) == punter) && !dist.contains(u)) {
          dist += (u -> (dist(v) + 1))
          deque.add(u)

          val d = distFromMines(start)(u)
          score += d * d
        }
      }

      // future
      sourceToTarget.get(start).foreach(target => if (distFromMines.contains(start) && distFromMines(start).contains(target)) {
        val d = distFromMines(start)(target)
        if (dist.contains(target)) {
          score += d * d * d
          logger.info(s"future bonus $punter: ${d * d * d}")
        } else {
          score -= d * d * d
          logger.info(s"future penalty $punter: ${d * d * d}")
        }
      })
    })

    score
  }
}
