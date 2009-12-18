package vmlang.compiler.typecheck

abstract class TypeCompliance
case object Complies extends TypeCompliance
abstract class DoesntComply extends TypeCompliance
case class NonexistentType(name:String) extends DoesntComply
case class DoesntDescend(expected:String, given:String) extends DoesntComply

class TypeErrors(errors:List[TypeError]) extends CompoundCompilerError(errors)
abstract class TypeError extends NormalCompilerError
class NonexistentTypeError(name:String) extends TypeError {
  def repr = "type " + name + " doesn't exist"
}
class DoesntDescendError(expected:String, given:String) extends TypeError {
  def repr = "given type " + given + " doesn't descend from expected type " + expected
}
