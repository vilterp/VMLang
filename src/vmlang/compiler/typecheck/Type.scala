package vmlang.compiler.typecheck

abstract class Type
case object UnknownType extends Type
abstract class KnownType extends Type {
  val name:String
  override def toString = name
}
case class AbsType(name:String) extends KnownType
abstract class ConcreteType extends KnownType
case class RefType(name:String) extends ConcreteType
case class PrimType(name:String) extends ConcreteType

case class FuncType(paramTypes:List[Type], returnType:Type) {
  override def toString = paramTypes.mkString("(",",",")") + " => " + returnType
}
      // TODO: make this part of the normal type hierarchy...
