package com.kenkoooo.sugoi

import java.io.File
import java.util.concurrent.TimeUnit

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.logging.log4j.scala.Logging

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, _}
import scala.sys.process.{Process, ProcessIO}

object SugoiDealer extends Logging {
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def main(args: Array[String]): Unit = {
    val filepath = args(0)
    val map = mapper.readValue[LambdaMap](new File(filepath), classOf[LambdaMap])
    val programs = for (i: Int <- 1 until args.length) yield new AiProgram(args(i), i)
    setup(programs, map)
    play(programs, map)
  }

  /**
    * セットアップをする
    *
    * @param programs AI たち
    * @param map      マップ
    */
  private def setup(programs: Seq[AiProgram], map: LambdaMap): Unit = {
    programs.foreach(program => {
      val setupInput = mapper.writeValueAsString(SetupToPunter(program.punter, programs.size, map))
      val (setupOutput, code) = program.put(setupInput, 10)
      if (code != 0) program.penaltyCount += 1
      val output = mapper.readValue[SetupToServer](setupOutput, classOf[SetupToServer])
      program.state = output.state
    })

  }

  /**
    * ゲームをプレイさせる
    *
    * @param programs AI たち
    * @param map      マップ
    */
  private def play(programs: Seq[AiProgram], map: LambdaMap): Unit = {
    val gameState = new GameState(map, programs.length)
    val deque = new ArrayBuffer[Move]()
    programs.foreach { p => deque.append(PassMove(Pass(p.punter))) }

    while (gameState.remainEdgeCount > 0) {
      def playOneTurn(p: AiProgram): Unit = {
        val playToPunterString = mapper.writeValueAsString(PlayToPunter(PreviousMoves(deque.toArray), p.state))
        deque.remove(0)

        // play
        val (playOutput, code) = p.put(playToPunterString, 2)

        if (p.penaltyCount >= 10 || code != 0) {
          // failed
          p.penaltyCount += 1
          deque.append(PassMove(Pass(p.punter)))
          return
        }
        val moveFromPunter = mapper.readValue[MoveFromPunter](playOutput, classOf[MoveFromPunter])
        p.state = moveFromPunter.state

        if (moveFromPunter.claim == null) {
          deque.append(PassMove(moveFromPunter.pass))
          return
        }

        val source = moveFromPunter.claim.source
        val target = moveFromPunter.claim.source
        if (gameState.isUsed(source, target)) {
          logger.error(s"$source -- $target is already used!!!")
          deque.append(PassMove(Pass(p.punter)))
        } else {
          logger.info(s"$source -- $target")
          gameState.addEdge(source, target, p.punter)
          deque.append(ClaimMove(moveFromPunter.claim))
        }
      }

      programs.foreach(p => playOneTurn(p))
    }
  }
}


class AiProgram(val command: String, val punter: Int) extends Logging {
  var penaltyCount = 0
  var state = ""

  /**
    * give the program the string as the standard input
    *
    * @param input a string given to the program via stdin
    * @return the output string and exit code
    */
  def put(input: String, timeoutSeconds: Long): (String, Int) = {
    val buffer = new ArrayBuffer[String]()
    val io = new ProcessIO(
      in => {
        val writer = new java.io.PrintWriter(in)
        writer.println(input)
        writer.close()
      },
      out => {
        val src = scala.io.Source.fromInputStream(out)
        for (line <- src.getLines()) {
          buffer.append(line)
        }
        src.close()
      },
      _.close())
    val process = Process(Array(command)).run(io)
    val future = Future(blocking(process.exitValue()))

    val result = try {
      Await.result(future, duration.Duration(timeoutSeconds, TimeUnit.SECONDS))
    } catch {
      case e: TimeoutException =>
        logger.catching(e)
        process.destroy()
        process.exitValue()
    }
    (buffer.mkString, result)
  }
}