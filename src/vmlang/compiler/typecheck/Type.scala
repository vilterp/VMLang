package vmlang.compiler.typecheck

import vmlang.compiler.ast._

abstract class Type {
  val name:String
  val numParams:Int
  override def toString = name
  def toTypeExpr = Parser.parseTypeExpr(toString)
}
case class AbsType(name:String, numParams:Int) extends Type
abstract class ConcreteType extends Type
case class RefType(name:String, numParams:Int) extends ConcreteType
case class PrimType(name:String) extends ConcreteType {
  val numParams = 0
}

/*

Type
  [AbsType]
  ConcreteType
    [RefType]
    [PrimType]

*/
