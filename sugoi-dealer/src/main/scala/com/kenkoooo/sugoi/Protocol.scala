package com.kenkoooo.sugoi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

class SetupToPunter(val punter: Int, val punters: Int, val map: Map)

class SetupToServer(val ready: Int, val state: String)

class River(val source: Int, val target: Int)

@JsonIgnoreProperties(ignoreUnknown = true)
class Site(val id: Int)

class Map(val sites: Array[Site], val rivers: Array[River], val mines: Array[Int])

class PlayToPunter(val move: PreviousMoves)

class PreviousMoves(val moves: Array[Move])

trait Move

trait MoveWithState {
  def state: String
}

class ClaimMove(val claim: Claim) extends Move

class ClaimMoveMoveWithState(val claim: Claim, val state: String) extends MoveWithState

class Claim(val punter: Int, val source: Int, val target: Int)

class PassMove(val pass: Pass) extends Move

class PassMoveMoveWithState(val pass: Pass, val state: String) extends MoveWithState

class Pass(val punter: Int)

class ScoreToPunter(val stop: Stop, val state: String)

class Stop(val moves: Array[Move], val scores: Array[Score])

class Score(val punter: Int, val score: Int)

class TimeoutToPunter(val timeout: Int)