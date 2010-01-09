package vmlang.assembler

import vmlang.common.{OptParser, FatalError}

import java.io.{File, FileOutputStream, FileNotFoundException, IOException}
import io.Source

object Main extends OptParser {
  
  def numArgs(n:Int) = n == 1
  val argErrorMsg = "supply 1 file to assemble"
  val knownFlags = List()
  val defaultOpts = Map[String,String]()
  val help = "usage: vmla <file to assemble>"
  
  def run(args:List[String], flags:List[String], opts:Map[String,String]):Unit =
      try {
        writeFile(Assemble(Parse(loadFile(args.head))), (args.head split '.')(0) + ".vmlc")
      } catch {
        case ParserError(msg) => throw FatalError("Parser Error: " + msg)
        case AssemblyError(msg) => throw FatalError("Assembly Error: " + msg)
      }
  
  def writeFile(output:List[Byte], fileName:String):Unit =
      try {
        new FileOutputStream(new File(fileName)).write(output.toArray)
      } catch {
        case e:FileNotFoundException => throw FatalError("Error creating file " + fileName)
        case e:IOException => throw FatalError("Error writing file " + fileName)
      }
  
  def loadFile(fileName:String):String =
      try {
        Source.fromFile(fileName).getLines.mkString
      } catch {
        case e:FileNotFoundException => throw FatalError("File not found: " + fileName)
        case e:IOException => throw FatalError("Error reading file " + fileName)
      }
  
}
