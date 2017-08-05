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
    val programs = for (i: Int <- 1 until args.length) yield new PunterProgram(args(i), i - 1, i - 1 == 0)
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
      val setupInput = mapper.writeValueAsString(SetupToPunter(program.punter, programs.size, map, LambdaSettings(true)))
      val (setupOutput, code) = program.putCommand(setupInput, 10)
      if (code != 0 || setupOutput == "") {
        program.penaltyCount += 1
        program.state = new ArrayBuffer[String]()
        futureBuffer.append(Array())
      } else {
        val output = mapper.readValue[SetupToServer](setupOutput, classOf[SetupToServer])
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
  private def play(programs: Seq[PunterProgram], gameState: GameState): ArrayBuffer[Move] = {
    val deque = new ArrayBuffer[Move]()
    programs.foreach { p => deque.append(PassMove(Pass(p.punter))) }


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
      val target = moveFromPunter.claim.target
      if (gameState.isUsed(source, target)) {
        logger.error(s"$source -- $target is already used!!!")
        deque.append(PassMove(Pass(p.punter)))
      } else {
        logger.info(s"$source -- $target")
        gameState.addEdge(source, target, p.punter)
        deque.append(ClaimMove(moveFromPunter.claim))
      }
    }

    for (_ <- 0 until gameState.edgeCount) programs.foreach(p => playOneTurn(p))
    deque
  }
}

class PunterProgram(cmd: String, val punter: Int, battler: Boolean = false) extends Logging with BattleLogging {
  var state: Object = _
  var penaltyCount = 0

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

  private def outputBattleLog(line: String): Unit = if (battler) battleLogger.info(line.trim)

  private def logSend(line: String): Unit = outputBattleLog(s"SEND $line")

  private def logRecv(line: String): Unit = outputBattleLog(s"RECV $line")

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
    try {
      // handshake
      val handshakeFromPunter = reader.next()

      logger.info(s"handshake from punter: $handshakeFromPunter")
      val name = SugoiMapper.mapper.readValue(handshakeFromPunter, classOf[HandShakeFromPunter]).me
      val handShakeFromServer = SugoiMapper.mapper.writeValueAsString(HandShakeFromServer(name)) + "\n"

      os.write(s"${handShakeFromServer.length}:$handShakeFromServer".getBytes)
      os.flush()

      logger.info(s"handshake to punter: ${handShakeFromServer.length}:$handShakeFromServer")

      val commandFromServer = command + "\n"
      os.write(s"${commandFromServer.length}:$commandFromServer".getBytes())
      os.flush()

      logRecv(SugoiMapper.purify(commandFromServer))
      logger.info(s"command to punter: ${commandFromServer.length}:$commandFromServer")

      val fromPunter = reader.next()
      logSend(SugoiMapper.purify(fromPunter))
      logger.info(s"command from punter: $fromPunter")
      (fromPunter, 0)
    } catch {
      case e: Throwable =>
        logger.catching(e)
        val err = new BufferedReader(new InputStreamReader(proc.getErrorStream))
        logger.error(err.lines().collect(Collectors.joining("\n")))
        err.close()
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