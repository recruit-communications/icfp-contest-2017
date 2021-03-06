package com.kenkoooo.sugoi

import java.io.{BufferedReader, File, InputStream, InputStreamReader}
import java.util.stream.Collectors

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.logging.log4j.scala.Logging
import org.apache.logging.log4j.{LogManager, Logger}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.{Duration, _}
import scala.language.postfixOps

trait BattleLogging {
  val battleLogger: Logger = LogManager.getLogger("BattleLog")
}

object SugoiDealer extends Logging with BattleLogging {
  val mapper: ObjectMapper = SugoiMapper.mapper

  def main(args: Array[String]): Unit = {
    logger.info("sugoi-dealer is starting...")
    val filepath = args(0)
    val map = mapper.readValue[LambdaMap](new File(filepath), classOf[LambdaMap])
    logger.info("lambda map loaded")
    val programs = for (i: Int <- 1 until args.length) yield new PunterProgram(args(i), i - 1, i - 1 == 0, map.mines.length)
    val futures = setup(programs, map)

    val gameState = new GameState(map, programs.length, futures)
    val deque = play(programs, gameState)

    val scores = new ArrayBuffer[Score]()
    gameState.calcScore().foreach(v => {
      val (punter, score) = v
      scores.append(Score(punter, score))
      logger.info(s"Player: $punter, Score: $score")
    })
    battleLogger.info(s"RECV ${mapper.writeValueAsString(ScoreToPunter(Stop(deque.toArray, scores.toArray)))}")
  }

  /**
    * setup
    *
    * @param programs AI programs
    * @param map      map
    */
  private def setup(programs: Seq[PunterProgram], map: LambdaMap): ArrayBuffer[Array[LambdaFuture]] = {
    val futureBuffer = new ArrayBuffer[Array[LambdaFuture]]()
    programs.foreach(program => {
      val setupInput = mapper.writeValueAsString(SetupToPunter(program.punter, programs.size, map, LambdaSettings(futures = true, splurges = true, options = true)))
      val (setupOutput, code) = program.putCommand(setupInput, 10)
      if (program.battler) battleLogger.info(s"RECV ${SugoiMapper.purify(setupInput)}")
      if (code != 0 || setupOutput == "") {
        program.penaltyCount += 1
        program.state = new ArrayBuffer[String]()
        futureBuffer.append(Array())
      } else {
        val output = mapper.readValue[SetupToServer](setupOutput, classOf[SetupToServer])
        if (program.battler) battleLogger.info(s"SEND ${SugoiMapper.purify(setupOutput)}")
        program.state = output.state
        futureBuffer.append(output.futures)
      }
    })
    futureBuffer
  }

  /**
    * play game
    *
    * @param programs  AI programs
    * @param gameState state of the game
    */
  def play(programs: Seq[PunterProgram], gameState: GameState): ArrayBuffer[Move] = {
    val deque = new ArrayBuffer[Move]()
    programs.foreach { p => deque.append(PassMove(Pass(p.punter))) }


    def playOneTurn(p: PunterProgram): Unit = {
      val playToPunterString = mapper.writeValueAsString(PlayToPunter(PreviousMoves(deque.toArray), p.state))
      deque.remove(0)

      def penaltyProcess(): Unit = {
        p.penaltyCount += 1
        deque.append(PassMove(Pass(p.punter)))
        if (p.battler) {
          battleLogger.info(s"RECV ${SugoiMapper.purify(playToPunterString)}")
          battleLogger.info(s"SEND ${mapper.writeValueAsString(PassMove(Pass(p.punter)))}")
        }
      }

      def logAndQ(move: Move): Unit = {
        if (p.battler) {
          battleLogger.info(s"RECV ${SugoiMapper.purify(playToPunterString)}")
          battleLogger.info(s"SEND ${mapper.writeValueAsString(move)}")
        }
        deque.append(move)
      }

      if (p.penaltyCount >= 10) {
        // dropout
        if (p.penaltyCount == 10) logger.error(s"punter ${p.punter} 10 times penalty")
        penaltyProcess()
        return
      }

      // play
      val (playOutput, code) = p.putCommand(playToPunterString, 2)

      if (code != 0) {
        // failed
        penaltyProcess()
        return
      }
      val moveFromPunter = mapper.readValue[MoveFromPunter](playOutput, classOf[MoveFromPunter])
      p.state = moveFromPunter.state

      if (moveFromPunter.pass != null) {
        // pass
        logAndQ(PassMove(moveFromPunter.pass))
        p.passCount += 1
      } else if (moveFromPunter.claim != null) {
        // claim
        val source = moveFromPunter.claim.source
        val target = moveFromPunter.claim.target
        if (gameState.isUsed(source, target)) {
          logger.error(s"punter ${p.punter}: $source -- $target is already used!!!")
          penaltyProcess()
        } else {
          logger.info(s"$source -- $target")
          gameState.addEdge(source, target, p.punter)
          logAndQ(ClaimMove(moveFromPunter.claim))
        }
      } else if (moveFromPunter.splurge != null) {
        // splurge
        val route = moveFromPunter.splurge.route

        // when pass n times, you can choose n+1 edges, the maximum splurge size will be n+2
        if (route.length > p.passCount + 2) {
          logger.error(s"punter ${p.punter}: too many splurge! pass: ${p.passCount}, splurge:${mapper.writeValueAsString(route)}")
          penaltyProcess()
          return
        }

        var optionCount = 0
        for (i <- 1 until route.length) {
          val source = route(i - 1)
          val target = route(i)
          if (gameState.isUsed(source, target)) {
            if (gameState.canBuy(source, target, p.punter)) {
              optionCount += 1
            } else {
              logger.error(s"punter ${p.punter}: $source -- $target is already used!!!")
              penaltyProcess()
              return
            }
          }
        }
        if (p.optionRemain < optionCount) {
          logger.error(s"punter ${p.punter}: lack of option")
          penaltyProcess()
          return
        }

        for (i <- 1 until route.length) {
          val source = route(i - 1)
          val target = route(i)
          logger.info(s"$source -- $target")
          if (!gameState.isUsed(source, target)) {
            gameState.addEdge(source, target, p.punter)
          } else {
            gameState.buyEdge(source, target, p.punter)
            p.optionRemain -= 1
          }
        }

        p.passCount -= route.length - 2
        logAndQ(SplurgeMove(moveFromPunter.splurge))
      } else if (moveFromPunter.option != null) {
        //option
        val s = moveFromPunter.option.source
        val t = moveFromPunter.option.target
        if (gameState.canBuy(s, t, p.punter) && p.optionRemain > 0) {
          gameState.buyEdge(s, t, p.punter)
          logAndQ(OptionMove(moveFromPunter.option))
          p.optionRemain -= 1
        } else {
          logger.error(s"${p.punter} can not option $s -- $t")
          penaltyProcess()
        }
      } else {
        // empty move
        logger.error(s"punter ${p.punter}: please specify claim, pass or splurge")
        penaltyProcess()
      }
    }

    var turn = 0
    while (turn < gameState.edgeCount) {
      programs.foreach(p => {
        if (turn < gameState.edgeCount) playOneTurn(p)
        turn += 1
      })
    }
    deque
  }
}

class PunterProgram(cmd: String, val punter: Int, val battler: Boolean = false, minesCount: Int) extends Logging with BattleLogging {
  var state: Object = _
  var penaltyCount = 0
  var passCount = 0
  var optionRemain: Int = minesCount

  /**
    * put a command to the program with timeout
    *
    * @param command command string
    * @param timeout timeout (seconds)
    * @return (Result, exit code)
    */
  def putCommand(command: String, timeout: Long): (String, Long) = {
    val f = Future {
      execute(command)
    }
    f.onComplete(content => content.get)

    try Await.result(f, Duration(timeout, SECONDS))
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
    val pb = new ProcessBuilder(cmd.split("\\s").toList.asJava)
    val proc = pb.start()
    val os = proc.getOutputStream
    val reader = new SugoiInputReader(proc.getInputStream)

    var lastCommand: String = ""
    try {
      // handshake
      val handshakeFromPunter = reader.next()

      logger.debug(s"handshake from punter: $handshakeFromPunter")
      val name = SugoiMapper.mapper.readValue(handshakeFromPunter, classOf[HandShakeFromPunter]).me
      val handShakeFromServer = SugoiMapper.mapper.writeValueAsString(HandShakeFromServer(name)) + "\n"

      lastCommand = s"${handShakeFromServer.length}:$handShakeFromServer"
      os.write(lastCommand.getBytes)
      os.flush()

      logger.debug(s"handshake to punter: ${handShakeFromServer.length}:$handShakeFromServer")

      val commandFromServer = command + "\n"

      lastCommand = s"${commandFromServer.length}:$commandFromServer"
      os.write(lastCommand.getBytes())
      os.flush()

      logger.debug(s"command to punter: ${commandFromServer.length}:$commandFromServer")

      val err = new BufferedReader(new InputStreamReader(proc.getErrorStream))
      logger.error(err.lines().collect(Collectors.joining("\n")))
      err.close()

      val fromPunter = reader.next()
      logger.debug(s"command from punter: $fromPunter")
      (fromPunter, 0)
    } catch {
      case e: Throwable =>
        logger.catching(e)
        logger.error(s"Last input from server: $lastCommand")
        ("", 1)
    } finally {
      reader.close()
      os.close()
      proc.destroy()
      logger.info("Process successfully destroyed")
    }
  }
}

/**
  * Input String Reader to communicate to the bridge
  *
  * @param in standard input stream
  */
class SugoiInputReader(in: InputStream) extends Logging {
  val reader = new InputStreamReader(in)

  def next(): String = {
    val buf = new ArrayBuffer[Char]()
    var r: Int = reader.read()
    while (r >= 0 && r != ':') {
      buf.append(r.toChar)
      r = reader.read()
    }

    val length = buf.mkString.toInt
    buf.clear()
    for (_ <- 0 until length) {
      buf.append(reader.read().toChar)
    }

    val line = buf.mkString
    logger.info(s"input > $line")
    line
  }

  def close(): Unit = reader.close()
}

object SugoiMapper {
  val mapper = new ObjectMapper()
  mapper.registerModules(DefaultScalaModule)

  def purify(line: String): String = mapper.writeValueAsString(mapper.readValue[PurifiedState](line, classOf[PurifiedState]))
}