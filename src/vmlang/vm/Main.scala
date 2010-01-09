package vmlang.vm

import java.io.{File, FileInputStream, FileNotFoundException, IOException}
import vmlang.common.{OptParser, FatalError}

object Main extends OptParser {
  
  def numArgs(n:Int) = n == 1
  val argErrorMsg = "supply 1 bytecode file to run"
  val knownFlags = List()
  val defaultOpts = Map("heap_size" -> "1024", "stack_size" -> "1024")
  val help =  "usage: vml <bytecode file> <options>\n" +
              "-heap_size=<num bytes>      heap size in bytes\n" +
              "-stack_size=<num bytes>     stack size in bytes"
  
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
      new VM(loadFileBytes(args(0)), heapSize, stackSize).run
    } catch {
      case e:InitError => throw FatalError("Couldn't start VM: " + e.message)
      case e:MalformedProgramError => throw FatalError("VM error: malformed program")
      case e:StackOverflowError => throw FatalError("VM error: stack overflow")
    }
  }
  
  def loadFileBytes(fileName:String):Array[Byte] = {
    try {
      val in = new FileInputStream(new File(fileName))
      val result = new Array[Byte](in.available)
      in.read(result)
      result        
    } catch {
      case e:FileNotFoundException =>
              throw FatalError("File not found: " + fileName)
      case e:IOException =>
              throw FatalError("Error reading file " + fileName)
    }
  }
  
}
