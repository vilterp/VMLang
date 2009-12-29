package vmlang.compiler.ast

abstract class ASTNode

case class Prog(defs:List[Def]) extends ASTNode
case class ParamSpec(name:String, argType:TypeExpr) extends ASTNode
case class TypeExpr(name:String, args:List[TypeExpr]) extends ASTNode {
  override val toString = if(isFunctionType)
                            args.init.mkString("(",",",")") + " => " + args.last
                          else
                            name + (if(args.isEmpty) "" else args.mkString("[",",","]"))
  val repr = if(isFunctionType) toString else name
  val isFunctionType = (name startsWith "Function") && !(name endsWith("Function"))
}

abstract class IPromptStmt extends ASTNode

case class Def(name:String, params:List[ParamSpec], returnType:TypeExpr, body:Expr) extends IPromptStmt

abstract class Expr extends IPromptStmt

case class IfExpr(condition:Expr, ifExpr:Expr, thenExpr:Expr) extends Expr
case class Call(name:String, args:List[Expr]) extends Expr

abstract class Atom extends Expr

case class IntLit(value:Int) extends Atom
case class FloatLit(value:Float) extends Atom
case class CharLit(value:Char) extends Atom
