package vmlang.compiler

import vmlang.common.optparser._

object Main extends OptParser {
  
  def numArgs(n:Int) = n == 1
  val argErrorMsg = "supply 1 file to compile"
  val knownFlags = List()
  val defaultOpts = Map[String,String]()
  val help = "usage: vmlc <source file> <options>"
  
  def run(args:List[String], flags:List[String], opts:Map[String,String]):Unit =
      try {
        writeFile(Compile(loadFile(args.head)), (args.head split '.')(0) + ".vmlc")
      } catch {
        case e:CompilerError => throw FatalError(e.repr)
      }
  
}
