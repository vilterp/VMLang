/*
package vmlang.compiler.typecheck

import ast._
import collection.immutable.{ Map, HashMap }

object TypeCheck {
  
  val typeTree =  TypeTree(AbsType("Value",0),List(
                      TypeTree(AbsType("Function",0),List(
                          TypeTree(RefType("Function0",1),Nil),
                          TypeTree(RefType("Function1",2),Nil),
                          TypeTree(RefType("Function2",3),Nil),
                          TypeTree(RefType("Function3",4),Nil),
                          TypeTree(RefType("Function4",5),Nil),
                          TypeTree(RefType("Function5",6),Nil),
                          TypeTree(RefType("Function6",7),Nil),
                          TypeTree(RefType("Function7",8),Nil),
                          TypeTree(RefType("Function8",9),Nil),
                          TypeTree(RefType("Function9",10),Nil))),
                      TypeTree(PrimType("Int",4),Nil),
                      TypeTree(PrimType("Bool",1),Nil),
                      TypeTree(PrimType("Null",0),Nil)))
  
  type FuncTable = Map[String,TypeExpr]
  type Scope     = Map[String,TypeExpr]
  
  val rootFuncTypes = new HashMap[String, TypeExpr] ++ (List(
    ("+"            ,      "(Int, Int) => Int"    ),
    ("-"            ,      "(Int, Int) => Int"    ),
    ("*"            ,      "(Int, Int) => Int"    ),
    ("/"            ,      "(Int, Int) => Int"    ),
    ("=="           ,      "(Int, Int) => Bool"   ),
    ("!="           ,      "(Int, Int) => Bool"   ),
    (">"            ,      "(Int, Int) => Bool"   ),
    (">="           ,      "(Int, Int) => Bool"   ),
    ("<"            ,      "(Int, Int) => Bool"   ),
    ("<="           ,      "(Int, Int) => Bool"   ),
    ("true"         ,      "() => Bool"           ),
    ("false"        ,      "() => Bool"           ),
    ("null"         ,      "() => Null"           ),
    ("and"          ,      "(Bool, Bool) => Bool" ),
    ("or"           ,      "(Bool, Bool) => Bool" ),
    ("not"          ,      "(Bool) => Bool"       ),
    ("printInt"     ,      "(Int) => Null"        )
  ) map { tp => (tp._1, Parse.parseTypeExpr(tp._2)) })
  
  def apply(e:Env):Env =
      (checkForValidMain(e) ::: checkCompliance(e)) match {
        case Nil => e
        case es  => throw new TypeErrors(es.removeDuplicates)
      }
  
  def checkForValidMain(e:Env):List[TypeError] =
      // check that main exists and is () => Null
      (e.defs get "main") match {
        case Some(d) => (d.returnType == TypeExpr("Null", Nil) && d.params == Nil) match {
          case true  => Nil
          case false => List(InvalidMainError(d.typeExpr))
        }
        case None => List(NoMainError())
      }
  
  def checkCompliance(e:Env):List[TypeError] =
      e.defs.toList flatMap { case (name, d) => checkDef(d, e) }
  
  def checkDef(d:Def, e:Env):List[TypeError] = {
    val scope = Map() ++ (d.params map { ps => (ps.name, ps.argType) })
    (d.params flatMap { ps => e.tt.checkValidTypeExpr(ps.argType) }) :::
      (e.tt.checkValidTypeExpr(d.returnType)) :::
        (e.tt.checkCompilableTypeExpr(d.returnType, { te => UncompilableTypeSpecError(te) })) :::
          (checkCalls(d.body, scope, e) match {
            case Nil => e.tt.complies(d.returnType, inferType(d.body, scope, e))
            case l   => l
          })
  }
  
  def checkCalls(ex:Expr, s:Scope, e:Env):List[TypeError] =
      ex match {
        case a:Atom              => Nil
        case IfExpr(cond, i, el) => checkCalls(List(cond, i, el), s, e)
        case c:Call              => checkCall(c, s, e)
      }
    
  def checkCalls(exprs:List[Expr], s:Scope, e:Env):List[TypeError] =
      exprs flatMap { expr => checkCalls(expr, s, e) }
  
  def checkCall(call:Call, scope:Scope, e:Env):List[TypeError] = {
    val giv = call.args
    (scope get call.name) match {
      case Some(_) => Nil
      case None    => (e.ft get call.name) match {
        case Some(funcType) => {
          val exp = funcType.args.init
          (if(exp.length == giv.length)
            Nil
          else
            List(WrongNumCallArgs(call.name, exp.length, giv.length))) :::
                                                checkArgCompliance(exp, giv, scope, e)
        }
        case None => NonexistentFuncError(call.name) :: checkCalls(call.args, scope, e)
      }
    }
  }
  
  def checkArgCompliance(expTypes:List[TypeExpr], args:List[Expr], s:Scope, e:Env):List[TypeError] =
      checkCalls(args.dropRight(expTypes.length - args.length), s, e) :::
      ((expTypes zip args) flatMap { case (ext, ex) => checkCalls(ex, s, e) match {
        case Nil =>
            val inferredTypeExpr = inferType(ex, s, e)
            e.tt.complies(ext, inferredTypeExpr) :::
              e.tt.checkCompilableTypeExpr(inferredTypeExpr, { te => UncompilableArgTypeError(te) })
        case l   => l
      } })
  
  def inferType(expr:Expr, e:Env):TypeExpr =
      checkCalls(expr, Map[String,TypeExpr](), e) match {
        case Nil => inferType(expr, Map[String,TypeExpr](), e)
        case es  => throw TypeErrors(es)
      }
  
  def inferType(expr:Expr, s:Scope, e:Env):TypeExpr =
      expr match {
        case IntLit(_)       => TypeExpr("Int",Nil)
        case CharLit(_)      => TypeExpr("Char",Nil)
        case FloatLit(_)     => TypeExpr("Float",Nil)
        case IfExpr(_, i, t) => e.tt.deepestCommonAncestor(inferType(i, s, e),
                                                         inferType(t, s, e))
        case c:Call          => if(isParam(c, s)) s(c.name) else e.ft(c.name).args.last
      }
  
  def isParam(call:Call, scope:Scope):Boolean =
      call.args.isEmpty && (scope isDefinedAt call.name)
  
}

*/