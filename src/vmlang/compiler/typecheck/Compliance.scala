package vmlang.compiler.typecheck

import vmlang.compiler.ast._

case class TypeErrors(errors:List[CompilerError]) extends CompoundCompilerError(errors)

abstract class TypeError extends NormalCompilerError

case class NonexistentType(name:String) extends TypeError {
  val repr = "Nonexistent type: \"" + name + "\""
}

case class Mismatch(expected:TypeExpr, given:TypeExpr) extends TypeError {
  val repr = "Type mismatch. Expected: " + expected.shortRepr + "; given: " + given.shortRepr
}

case class WrongNumTypeArgs(expected:Int, given:Int) extends TypeError {
  val repr = "Wrong number of type arguments. Expected: " + expected + "; given: " + given
}

case class WrongNumCallArgs(expected:Int, given:Int) extends TypeError {
  val repr = "Wrong number of arguments. Expected: " + expected + "; given: " + given
}
