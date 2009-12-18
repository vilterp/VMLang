package vmlang.compiler.typecheck

import vmlang.compiler.ast._
import collection.immutable.Map

object TypeCheck {
  
  type FuncTable = Map[String,FuncType]
  
  val typeTree =  TypeTree(AbsType("Value"),List(
                      TypeTree(AbsType("Ord"),List(
                          TypeTree(AbsType("Num"),List(
                              TypeTree(PrimType("Int"),Nil),
                              TypeTree(PrimType("Float"),Nil))),
                          TypeTree(PrimType("Char"),Nil)))))
  
  // def apply(prog:Prog, funcTypes:Map[String,FuncType], typeTree:TypeTree) = 
  // 
  // def checkFunc(func:Def, funcTypes:Map[String,FuncType], typeTree:TypeTree) =
  
  def inferType(expr:Expr, funcTypes:FuncTable, typeTree:TypeTree):Type = expr match {
    case IntLit(_) => typeTree find "Int"
    case CharLit(_) => typeTree find "Char"
    case IfExpr(_,i,t) => typeTree.deepestCommonAncestor(inferType(i,funcTypes,typeTree),
                                                         inferType(t,funcTypes,typeTree))
    case Call(name,args) => funcTypes get name match {
      case Some(ft) => ft.returnType
      case None => UnknownType
    }
  }
  
}