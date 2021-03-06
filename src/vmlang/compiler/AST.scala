package vmlang.compiler.ast

import vmlang.compiler.typecheck.Type

abstract class ASTNode

case class Prog(defs:Map[String, Def]) extends ASTNode
case class ParamSpec(name:String, argType:TypeExpr) extends ASTNode
case class TypeExpr(name:String, args:List[TypeExpr]) extends ASTNode {
  val isFunctionType = (name startsWith "Function") && !(name endsWith("Function"))
  override val toString = if(isFunctionType)
                            args.init.mkString("(",",",")") + " => " + args.last
                          else
                            name + (if(args.isEmpty) "" else args.mkString("[",",","]"))
  val repr = if(isFunctionType) toString else name
}

abstract class REPLStmt extends ASTNode

case class Def(name:String, params:List[ParamSpec], returnType:TypeExpr, body:Expr) extends REPLStmt {
  val typeExpr = TypeExpr("Function" + params.length, (params map { _.argType }) ::: List(returnType))
}

abstract class Expr extends REPLStmt

case class IfExpr(condition:Expr, ifExpr:Expr, thenExpr:Expr) extends Expr
case class Call(name:String, args:List[Expr]) extends Expr
case class TypedCall(call:Call, types:List[Type])

abstract class Atom extends Expr

case class IntLit(value:Int) extends Atom
case class FloatLit(value:Float) extends Atom
case class CharLit(value:Char) extends Atom
