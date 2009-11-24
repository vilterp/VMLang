package vmlang.compiler.ast

abstract class ASTNode

case class Prog(defs:List[Def]) extends ASTNode
case class Def(name:String, args:Option[List[ArgSpec]],
               returnType:Option[Type], body:Expr) extends ASTNode
case class ArgSpec(name:String, argType:Type) extends ASTNode
case class Type(name:String,params:Option[List[Type]]) extends ASTNode


abstract class Expr extends ASTNode

case class IfExpr(condition:Expr, ifExpr:Expr, thenExpr:Expr) extends Expr
case class Call(name:String, args:List[Expr]) extends Expr

abstract class Atom extends Expr

case class Integer(value:String) extends Atom
case class Char(value:Char) extends Atom
case object EmptyList extends Atom