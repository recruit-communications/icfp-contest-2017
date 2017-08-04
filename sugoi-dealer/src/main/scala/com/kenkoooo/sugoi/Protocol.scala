package com.kenkoooo.sugoi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

case class SetupToPunter(punter: Int, punters: Int, map: LambdaMap)

case class SetupToServer(ready: Int, state: String)

case class River(source: Int, target: Int)

@JsonIgnoreProperties(ignoreUnknown = true)
case class Site(id: Int)

case class LambdaMap(sites: Array[Site], rivers: Array[River], mines: Array[Int])

case class PlayToPunter(move: PreviousMoves, state: String)

case class PreviousMoves(moves: Array[Move])

trait Move

case class ClaimMove(claim: Claim) extends Move

case class Claim(punter: Int, source: Int, target: Int)

case class PassMove(pass: Pass) extends Move

case class Pass(punter: Int)

case class MoveFromPunter(claim: Claim, pass: Pass, state: String)

case class ScoreToPunter(stop: Stop, state: String)

case class Stop(moves: Array[Move], scores: Array[Score])

case class Score(punter: Int, score: Int)

case class TimeoutToPunter(timeout: Int)