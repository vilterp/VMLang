package vmlang.compiler.typecheck

import vmlang.compiler.ast._
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
  ) map { tp => (tp._1, Parser.parseTypeExpr(tp._2)) })
  
  def apply(prog:Prog):Map[String,CheckedDef] =
      apply(prog, rootFuncTypes, typeTree)
  
  def apply(prog:Prog, ft:FuncTable, tt:TypeTree):Map[String,CheckedDef] =
      (checkForMain(prog) ::: checkCompliance(prog, addTypeSigs(ft, prog), tt)) match {
        case Nil => Map[String,CheckedDef]() ++ (prog.defs map { 
          case (n, d) => (n -> CheckedDef(d.params map { ps => {
            (ps.name, (tt find ps.argType.name).size)
          } }, d.body) )
        })
        case l   => throw new TypeErrors(l)
      }
  
  private def checkForMain(prog:Prog):List[TypeError] =
      (prog.defs get "main") match {
        case Some(d) => (d.returnType == TypeExpr("Null",Nil) && d.params == Nil) match {
          case true  => Nil
          case false => List(InvalidMainError(mkFuncTypeExpr(d)))
        }
        case None => List(NoMainError)
      }
  
  private def addTypeSigs(ft:FuncTable, p:Prog):FuncTable =
      p.defs.foldLeft(ft){ (ft, d) => ft + (d._1 -> mkFuncTypeExpr(d._2)) }
  
  private def mkFuncTypeExpr(d:Def):TypeExpr =
      TypeExpr("Function" + d.params.length, (d.params map { _.argType }) ::: List(d.returnType))
  
  private def checkCompliance(p:Prog, ft:FuncTable, tt:TypeTree):List[TypeError] =
      p.defs.toList flatMap { m => checkDef(m._2, ft, tt) }
  
  private def checkDef(d:Def, ft:FuncTable, tt:TypeTree):List[TypeError] = {
    val s = Map() ++ (d.params map { ps => (ps.name, ps.argType) })
    checkCalls(d.body, s, ft, tt) match {
      case Nil => tt.complies(d.returnType, inferType(d.body, s, ft, tt))
      case l   => l
    }
  }
  
  private def checkCalls(e:Expr, s:Scope, ft:FuncTable, tt:TypeTree):List[TypeError] =
      e match {
        case a:Atom          => Nil
        case IfExpr(i, c, e) => checkCalls(List(i, c, e), s, ft, tt)
        case c:Call          => checkCall(c, s, ft, tt) ::: checkCalls(c.args, s, ft, tt)
      }
    
  private def checkCalls(es:List[Expr], s:Scope, ft:FuncTable, tt:TypeTree):List[TypeError] =
      es flatMap { e => checkCalls(e, s, ft, tt) }
  
  def checkCall(call:Call, scope:Scope, ft:FuncTable, tt:TypeTree):List[TypeError] = {
    val giv = call.args
    (scope get call.name) match {
      case Some(_) => Nil
      case None    => (ft get call.name) match {
        case Some(funcType) => {
          val exp = funcType.args.init
          (if(exp.length == giv.length) Nil else List(WrongNumCallArgs(exp.length, giv.length))) :::
                                                            checkArgCompliance(exp, giv, scope, ft, tt)
        }
        case None => List(NonexistentFuncError(call.name))
      }
    }
  }
  
  private def checkArgCompliance(expTypes:List[TypeExpr], args:List[Expr],
                                        s:Scope, ft:FuncTable, tt:TypeTree):List[TypeError] =
      (expTypes zip args) flatMap { case (et, e) => checkCalls(e, s, ft, tt) match {
        case Nil => tt.complies(et, inferType(e, s, ft, tt))
        case l   => l
      } }
  
  def inferType(expr:Expr, ft:FuncTable, tt:TypeTree):TypeExpr =
      inferType(expr, Map(), ft, tt)
  
  def inferType(expr:Expr, scope:Scope, ft:FuncTable, tt:TypeTree):TypeExpr = {
    expr match {
      case IntLit(_)       => TypeExpr("Int",Nil)
      case CharLit(_)      => TypeExpr("Char",Nil)
      case FloatLit(_)     => TypeExpr("Float",Nil)
      case IfExpr(_, i, t) => tt.deepestCommonAncestor(inferType(i, ft, tt),
                                                       inferType(t, ft, tt))
      case c:Call          => if(isParam(c, scope)) scope(c.name) else ft(c.name).args.last
    }
  }
  
  private def isParam(call:Call, scope:Scope):Boolean =
      call.args.isEmpty && (scope isDefinedAt call.name)
  
}
