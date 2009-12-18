package vmlang.compiler.ast

abstract class ASTNode

case class Prog(defs:List[Def]) extends ASTNode
case class Def(name:String, args:List[ArgSpec],
               returnType:Option[TypeExpr], body:Expr) extends ASTNode
case class ArgSpec(name:String, argType:TypeExpr) extends ASTNode
case class TypeExpr(name:String) extends ASTNode

abstract class Expr extends ASTNode

case class IfExpr(condition:Expr, ifExpr:Expr, thenExpr:Expr) extends Expr
case class Call(name:String, args:List[Expr]) extends Expr

abstract class Atom extends Expr

case class IntLit(value:BigInt) extends Atom
case class CharLit(value:Char) extends Atom
case object EmptyList extends Atom

