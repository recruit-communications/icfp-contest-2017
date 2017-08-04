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
    val map = mapper.readValue[Map](new File(filepath), classOf[Map])
    val programs = for (i: Int <- 1 until args.length) yield new AiProgram(args(i), i)
    play(programs, map)
  }

  def play(programs: Seq[AiProgram], map: Map): Unit = {
    programs.foreach(program => {
      val setupInput = mapper.writeValueAsString(new SetupToPunter(program.punter, programs.size, map))
      val (setupOutput, code) = program.put(setupInput, 10)
      if (code != 0) program.penaltyCount += 1
      val output = mapper.readValue[SetupToServer](setupOutput, classOf[SetupToServer])
      program.state = output.state
    })

    val deque = new ArrayBuffer[Move]()

    programs.foreach { p => deque.append(new PassMove(new Pass(p.punter))) }
    while (true) {
      // TODO ループが終わるようにする
      programs.foreach(p => {
        val input = mapper.writeValueAsString(new PlayToPunter(new PreviousMoves(deque.toArray)))
        deque.remove(0)

        val (playOutput, code) = p.put(input, 2)

        if (p.penaltyCount >= 10 || code != 0) {
          p.penaltyCount += 1
          deque.append(new PassMove(new Pass(p.punter)))
        } else if (isClaim(playOutput)) {
          val output = mapper.readValue[ClaimMoveMoveWithState](playOutput, classOf[ClaimMoveMoveWithState])
          p.state = output.state
          deque.append(new ClaimMove(output.claim))

          // TODO スコアを得る
        } else {
          val output = mapper.readValue[PassMoveMoveWithState](playOutput, classOf[PassMoveMoveWithState])
          p.state = output.state
          deque.append(new PassMove(output.pass))
        }
      })
    }
  }

  private def isClaim(output: String): Boolean = {
    try {
      mapper.readValue[ClaimMoveMoveWithState](output, classOf[ClaimMoveMoveWithState])
      true
    } catch {
      case _: Throwable => false
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