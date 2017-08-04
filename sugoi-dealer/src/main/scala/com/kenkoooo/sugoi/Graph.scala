package com.kenkoooo.sugoi


import java.util

import scala.collection.mutable

object Graph {
  val UNUSED: Int = -1
}

class Graph(map: LambdaMap) {
  type Punter = Int
  type Vertex = Int
  val adj = new mutable.TreeMap[Vertex, mutable.TreeMap[Vertex, Punter]]
  map.sites.foreach(site => adj += (site.id -> new mutable.TreeMap[Vertex, Punter]()))
  map.rivers.foreach(river => {
    adj(river.source).put(river.target, Graph.UNUSED)
    adj(river.target).put(river.source, Graph.UNUSED)
  })
  val mines: Set[Vertex] = map.mines.toSet

  var remainEdgeCount: Int = map.rivers.length

  def isUsed(source: Vertex, target: Vertex): Boolean = adj(source)(target) != Graph.UNUSED

  /**
    * use edge
    *
    * @param source left side of the edge
    * @param target right side of the edge
    * @param punter user of this edge
    */
  def addEdge(source: Vertex, target: Vertex, punter: Punter): Unit = {
    adj(source)(target) = punter
    adj(target)(source) = punter
    remainEdgeCount -= 1
  }

  def calcScore(): mutable.TreeMap[Punter, Long] = {
    val map = new mutable.TreeMap[Punter, Long]()
    mines.foreach(from => {
      val dist = new mutable.TreeMap[Vertex, Int]()
      dist += (from -> 0)

      val deque = new util.ArrayDeque[Vertex]()
      deque.add(from)
      while (!deque.isEmpty) {
        val v = deque.poll()
        for ((u, p) <- adj(v)) {
          val du = dist.getOrElse(u, Integer.MAX_VALUE)
          if (du > dist(v) + 1) {
            dist += (u -> (dist(v) + 1))
            deque.add(u)
          }
        }
      }

    })
  }
}
