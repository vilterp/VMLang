package vmlang.assembler

import vmlang.common.optparser._

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
  
}
