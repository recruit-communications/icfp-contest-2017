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
  def main(args: Array[String]): Unit = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val filepath = args(0)
    val map = mapper.readValue[Map](new File(filepath), classOf[Map])
    

    val programs = for (i: Int <- 1 until args.length) yield new AiProgram(args(i))
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