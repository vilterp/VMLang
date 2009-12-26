package vmlang.compiler.ast

abstract class ASTNode

case class Prog(defs:List[Def]) extends ASTNode
case class Def(name:String, params:List[ParamSpec], returnType:TypeExpr, body:Expr) extends ASTNode
case class ParamSpec(name:String, argType:TypeExpr) extends ASTNode

abstract class TypeExpr extends ASTNode {
  val shortRepr:String
}
case class NormalTypeExpr(name:String, args:List[TypeExpr]) extends TypeExpr {
  override val toString = name + (if(args.isEmpty) "" else args.mkString("[",",","]"))
  val shortRepr = name
}
case class FuncTypeExpr(paramTypes:List[TypeExpr], returnType:TypeExpr) extends TypeExpr {
  override val toString = paramTypes.mkString("(",",",")") + " => " + returnType
  val shortRepr = toString
}

abstract class Expr extends ASTNode

case class IfExpr(condition:Expr, ifExpr:Expr, thenExpr:Expr) extends Expr
case class Call(name:String, args:List[Expr]) extends Expr

abstract class Atom extends Expr

case class IntLit(value:Int) extends Atom
case class FloatLit(value:Float) extends Atom
case class CharLit(value:Char) extends Atom
case object EmptyList extends Atom

