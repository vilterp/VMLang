package vmlang.compiler.ast

abstract class ASTNode

case class Prog(defs:List[Def]) extends ASTNode
case class Def(name:String, args:List[ArgSpec], returnType:TypeExpr, body:Expr) extends ASTNode
case class ArgSpec(name:String, argType:TypeExpr) extends ASTNode
case class TypeExpr(name:String) extends ASTNode

abstract class Expr extends ASTNode

case class IfExpr(condition:Expr, ifExpr:Expr, thenExpr:Expr) extends Expr
case class Call(name:String, args:List[Expr]) extends Expr

abstract class Atom extends Expr

case class IntLit(value:Int) extends Atom
case class FloatLit(value:Float) extends Atom
case class CharLit(value:Char) extends Atom
case object EmptyList extends Atom

