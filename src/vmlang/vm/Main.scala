package vmlang.vm

import vmlang.common.optparser._

object Main extends OptParser {
  
  def numArgs(n:Int) = n == 1
  val argErrorMsg = "supply 1 bytecode file to run"
  val knownFlags = List("d", "t")
  val defaultOpts = Map("heap_size" -> "1024", "stack_size" -> "1024")
  val help =  "usage: vml <bytecode file> <options>\n" +
              "-heap_size=<num bytes>      heap size in bytes\n" +
              "-stack_size=<num bytes>     stack size in bytes\n" +
              "-d                          debug\n" +
              "-t                          time"
  
  def run(args:List[String], flags:List[String], opts:Map[String,String]):Unit = {
    val heapSize = try {
      opts("heap_size").toInt
    } catch {
      case e:NumberFormatException => throw FatalError("Invalid heap size argument")
    }
    val stackSize = try {
      opts("stack_size").toInt
    } catch {
      case e:NumberFormatException => throw FatalError("Invalid stack size argument")
    }
    // business time
    try {
      val start = System.currentTimeMillis
      val cycles = new VM(loadFileBytes(args(0)), heapSize, stackSize, flags contains "d").run
      if(flags contains "t") printStats(System.currentTimeMillis - start, cycles)
    } catch {
      case e:InitError => throw FatalError("Couldn't start VM: " + e.message)
      case e:MalformedProgramError => throw FatalError("VM error: malformed program")
      case e:StackOverflowError => throw FatalError("VM error: stack overflow")
    }
  }
  
  def printStats(elapsedTime:Long, cycles:Long):Unit = {
    println
    val secs = elapsedTime.asInstanceOf[Float] / 1000
    println(secs + " seconds elapsed")
    println(cycles + " instructions run")
    val instsPerSec = cycles.asInstanceOf[Float] / secs
    println(instsPerSec + " instructions/sec (" + instsPerSec / 1e6 + " MHz)")
  }
  
}
