package com.kenkoooo.sugoi

import java.io.File
import java.util.Scanner

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.logging.log4j.scala.Logging

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object SugoiDealer extends Logging {
  val mapper: ObjectMapper = SugoiMapper.mapper

  def main(args: Array[String]): Unit = {
    val filepath = args(0)
    val map = mapper.readValue[LambdaMap](new File(filepath), classOf[LambdaMap])
    val programs = for (i: Int <- 1 until args.length) yield new PunterProgram(args(i), i)
    setup(programs, map)
    play(programs, map)
  }

  /**
    * setup
    *
    * @param programs AI programs
    * @param map      map
    */
  private def setup(programs: Seq[PunterProgram], map: LambdaMap): Unit = {
    programs.foreach(program => {
      val setupInput = mapper.writeValueAsString(SetupToPunter(program.punter, programs.size, map))
      val (setupOutput, code) = program.putCommand(setupInput, 10)
      if (code != 0) program.penaltyCount += 1
      val output = mapper.readValue[SetupToServer](setupOutput, classOf[SetupToServer])
      program.state = output.state
    })

  }

  /**
    * play game
    *
    * @param programs AI programs
    * @param map      Map
    */
  private def play(programs: Seq[PunterProgram], map: LambdaMap): Unit = {
    val gameState = new GameState(map, programs.length)
    val deque = new ArrayBuffer[Move]()
    programs.foreach { p => deque.append(PassMove(Pass(p.punter))) }

    while (gameState.remainEdgeCount > 0) {
      def playOneTurn(p: PunterProgram): Unit = {
        val playToPunterString = mapper.writeValueAsString(PlayToPunter(PreviousMoves(deque.toArray), p.state))
        deque.remove(0)

        // play
        val (playOutput, code) = p.putCommand(playToPunterString, 2)

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

class PunterProgram(cmd: String, val punter: Int) extends Logging {
  var state = ""
  var penaltyCount = 0

  def putCommand(command: String, timeout: Long): (String, Long) = {
    val f = future {
      execute(command)
    }
    f.onComplete(content => content.get)

    try Await.result(f, 10 seconds)
    catch {
      case e: TimeoutException =>
        logger.catching(e)
        ("", 1)
    }
  }

  /**
    * execute handshake and command
    *
    * @param command executable command string
    * @return response string and exit code
    */
  private def execute(command: String): (String, Long) = {
    val pb = new ProcessBuilder(Array(cmd).toList.asJava)
    val proc = pb.start()
    val os = proc.getOutputStream
    val sc = new Scanner(proc.getInputStream)
    try {
      // handshake
      val handshakeFromPunter = sc.nextLine()
      logger.info(s"handshake from punter: $handshakeFromPunter")
      val name = SugoiMapper.mapper.readValue(handshakeFromPunter, classOf[HandShakeFromPunter]).me
      os.write(SugoiMapper.mapper.writeValueAsBytes(HandShakeFromServer(name)))
      os.flush()

      os.write(command.getBytes)
      os.write("\n".getBytes())
      os.flush()

      val fromPunter = sc.nextLine()
      logger.info(s"command from punter: $fromPunter")
      (fromPunter, 0)
    } catch {
      case e: Throwable =>
        logger.catching(e)
        ("", 1)
    } finally {
      sc.close()
      os.close()
      proc.destroy()
      logger.info("Process successfully destroyed")
    }
  }
}

object SugoiMapper {
  val mapper = new ObjectMapper()
  mapper.registerModules(DefaultScalaModule)
}