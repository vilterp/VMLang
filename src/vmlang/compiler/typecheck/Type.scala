package vmlang.compiler.typecheck

import vmlang.compiler.ast._

abstract class Type
case class FuncType(paramTypes:List[TypeExpr], returnType:TypeExpr) extends Type {
  override def toString = paramTypes.mkString("(",",",")") + " => " + returnType
}
abstract class ExprType extends Type {
  val name:String
  val numParams:Int
  override def toString = name
  def toTypeExpr = Parser.parseTypeExpr(toString)
}
case class AbsType(name:String, numParams:Int) extends ExprType
abstract class ConcreteType extends ExprType
case class RefType(name:String, numParams:Int) extends ConcreteType
case class PrimType(name:String) extends ConcreteType {
  val numParams = 0
}

/*

Type
  [FuncType]
  ExprType
    [AbsType]
    ConcreteType
      [RefType]
      [PrimType]

*/