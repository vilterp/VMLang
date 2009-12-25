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
  val numErrors = errors.length
  val repr:String = (errors map { _.repr } ) mkString "\n"
}

case class NonexistentFuncError(funcName:String) extends NormalCompilerError {
  val repr = "Call to nonexistent function " + funcName
}
