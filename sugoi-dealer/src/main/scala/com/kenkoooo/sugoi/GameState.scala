package com.kenkoooo.sugoi


import java.util

import scala.collection.mutable.ArrayBuffer
import scala.collection.{immutable, mutable}

object GameState {
  val UNUSED: Int = -1
}

class GameState(map: LambdaMap, punterNum: Int, futures: ArrayBuffer[Array[LambdaFuture]]) {
  type Punter = Int
  type Vertex = Int
  type Score = Long

  val futureVector: immutable.IndexedSeq[mutable.TreeMap[Vertex, Vertex]] = for (_ <- 0 until punterNum) yield new mutable.TreeMap[Vertex, Vertex]()
  futures.zipWithIndex.foreach(v => {
    val (arr, i) = v
    Option(arr).foreach(_.foreach(f => futureVector(i) += (f.source -> f.target)))
  })

  val graph = new mutable.TreeMap[Vertex, mutable.TreeMap[Vertex, Punter]]
  map.sites.foreach(site => graph += (site.id -> new mutable.TreeMap[Vertex, Punter]()))
  map.rivers.foreach(river => {
    graph(river.source).put(river.target, GameState.UNUSED)
    graph(river.target).put(river.source, GameState.UNUSED)
  })
  val mines: Set[Vertex] = map.mines.toSet

  var edgeCount: Int = map.rivers.length

  def isUsed(source: Vertex, target: Vertex): Boolean = {
    graph.contains(source) && graph(source).contains(target) && graph(source)(target) != GameState.UNUSED
  }

  /**
    * use edge
    *
    * @param source left side of the edge
    * @param target right side of the edge
    * @param punter user of this edge
    */
  def addEdge(source: Vertex, target: Vertex, punter: Punter): Unit = {
    graph(source)(target) = punter
    graph(target)(source) = punter
  }

  /**
    * calculate scores for all players
    *
    * @return Map[Player, Score]
    */
  def calcScore(): mutable.TreeMap[Punter, Score] = {
    val map = new mutable.TreeMap[Punter, Score]()
    for (punter <- 0 until punterNum) map += (punter -> calcForOne(punter))
    map
  }

  private def calcForOne(punter: Punter): Score = {
    val sourceToTarget = futureVector(punter)

    var score: Score = 0
    mines.foreach(start => {
      val mapOption = sourceToTarget.get(start)

      // naive BFS
      val dist = new mutable.TreeMap[Vertex, Int]()
      dist += (start -> 0)
      val deque = new util.ArrayDeque[Vertex]()
      deque.add(start)
      while (!deque.isEmpty) {
        val v = deque.poll()
        for ((u, p) <- graph(v)) {
          if (p == punter) {
            val du = dist.getOrElse(u, Integer.MAX_VALUE)
            if (du > dist(v) + 1) {
              dist += (u -> (dist(v) + 1))
              deque.add(u)

              val s = dist(v) + 1
              score += (s * s)

              mapOption.foreach(target => {
                if (target == u) {
                  score += (s * s * s)
                } else {
                  score -= (s * s * s)
                }
              })
            }
          }
        }
      }
    })

    score
  }
}
