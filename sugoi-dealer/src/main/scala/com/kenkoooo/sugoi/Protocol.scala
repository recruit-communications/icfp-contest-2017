package com.kenkoooo.sugoi

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonInclude}

case class HandShakeFromPunter(me: String)

case class HandShakeFromServer(you: String)

case class SetupToPunter(punter: Int, punters: Int, map: LambdaMap, settings: LambdaSettings)

case class LambdaSettings(futures: Boolean, splurges: Boolean, options: Boolean)

case class LambdaFuture(source: Int, target: Int)

case class SetupToServer(ready: Int, state: Object, futures: Array[LambdaFuture])

case class River(source: Int, target: Int)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
case class Site(id: Int, x: Double, y: Double)

case class LambdaMap(sites: Array[Site], rivers: Array[River], mines: Array[Int])

case class PlayToPunter(move: PreviousMoves, state: Object)

case class PreviousMoves(moves: Array[Move])

trait Move

case class ClaimMove(claim: Claim) extends Move

case class Claim(punter: Int, source: Int, target: Int)

case class PassMove(pass: Pass) extends Move

case class Pass(punter: Int)

case class SplurgeMove(splurge: Splurge) extends Move

case class Splurge(punter: Int, route: Array[Int])

case class OptionMove(option: LambdaOption) extends Move

case class LambdaOption(punter: Int, source: Int, target: Int)

case class MoveFromPunter(claim: Claim, pass: Pass, splurge: Splurge, option: LambdaOption, state: Object)

case class ScoreToPunter(stop: Stop)

case class Stop(moves: Array[Move], scores: Array[Score])

case class Score(punter: Int, score: Long)

case class TimeoutToPunter(timeout: Int)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
case class PurifiedState(claim: Object, pass: Object, splurge: Object, option: Object, move: Object, ready: Object, futures: Object, punter: Object, punters: Object, map: Object, settings: Object)
