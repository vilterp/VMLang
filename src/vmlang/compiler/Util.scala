package vmlang.compiler

abstract class CompilerError extends Exception {
  val numErrors:Int
  val repr:String
}
// TODO: should have location, but can't implement - scala's combinator library drops it

abstract class NormalCompilerError extends CompilerError {
  val numErrors = 1
}

abstract class CompoundCompilerError(errors:List[CompilerError]) {
  def numErrors = errors.length
  def repr:String = (errors flatMap { _.repr } ) mkString "\n"
}

class NonexistentFuncError(funcName:String,inDef:String) extends NormalCompilerError {
  val repr = "reference to nonexistent function " + funcName + " in " + inDef
}