package vmlang.compiler.typecheck

import vmlang.compiler.ast.TypeExpr

case class TypeErrors(errors:List[CompilerError]) extends CompoundCompilerError(errors)

abstract class TypeError extends NormalCompilerError

case class NonexistentType(name:String) extends TypeError {
  val repr = "Nonexistent type: \"" + name + "\""
}

case class Mismatch(expected:TypeExpr, given:TypeExpr) extends TypeError {
  val repr = "Type mismatch. Expected: " + expected.repr + "; given: " + given.repr
}

case class WrongNumTypeArgs(expected:Int, given:Int) extends TypeError {
  val repr = "Wrong number of type arguments. Expected: " + expected + "; given: " + given
}

case class WrongNumCallArgs(func:String, expected:Int, given:Int) extends TypeError {
  val repr = "Wrong number of arguments for " + func + ". Expected: " + expected + "; given: " + given
}

case class NonexistentFuncError(funcName:String) extends TypeError {
  val repr = "Call to nonexistent function " + funcName
}

case class DuplicateDefError(name:String) extends TypeError {
  val repr = "Function " + name + " already defined. Taking first definition."
}

case object NoMainError extends TypeError {
  val repr = "No main function. (Must be () => Null)"
}

case class InvalidMainError(te:TypeExpr) extends TypeError {
  val repr = "Function main is " + te + "; must be () => Null"
}
