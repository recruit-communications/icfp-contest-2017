package com.kenkoooo.sugoi

import java.util.concurrent.TimeUnit

import org.apache.logging.log4j.scala.Logging

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, _}
import scala.sys.process.{Process, ProcessIO}

object SugoiDealer extends Logging {
  def main(args: Array[String]): Unit = {
    val mapFilepath = args(0)
    val punterNum = args.length - 1
    val x = for (i: Int <- 1 until args.length) yield new AiProgram(args(i))
    x.foreach(p => logger.info(p.command))
  }
}

class AiProgram(val command: String) extends Logging {

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