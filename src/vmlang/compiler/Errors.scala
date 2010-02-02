package vmlang.compiler

import util.parsing.input.Position

abstract class CompilerError extends Exception {
  val numErrors:Int
  val show:String
  def showPos(pos:Position):String = "[" + pos.toString + "]"
}

abstract class SingleCompilerError extends CompilerError {
  val numErrors = 1
  val message:String
}

abstract class SinglePosCompilerError extends SingleCompilerError {
  val pos:Position
  val show = message + " " + showPos(pos) + "\n" + pos.longString
}

abstract class MultPosCompilerError extends SingleCompilerError {
  val positions:List[Position]
  val message:String
  val show = message + ":\n" + (positions map { p => "\t" + showPos(p) + "\n\t" + p.longString })
}

abstract class CompoundCompilerError(errors:List[CompilerError]) extends CompilerError {
  val numErrors = errors.length
  val show:String = (errors map { _.show } ) mkString "\n"
}
