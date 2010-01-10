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
  ) map { tp => (tp._1, Parse.parseTypeExpr(tp._2)) })
  
  def apply(defs:List[Def]):Map[String,Def] =
      apply(defs, rootFuncTypes, typeTree)
  
  def apply(defs:List[Def], ft:FuncTable, tt:TypeTree):Map[String,Def] = {
    val (ds, dupErrors) = checkForDuplicates(defs)
    (dupErrors ::: checkForValidMain(ds) ::: checkCompliance(ds, addTypeSigs(ft, defs), tt)) match {
      case Nil => Map[String,Def]() ++ (ds map { d => (d.name, d) })
      case es  => throw new TypeErrors(es.removeDuplicates)
    }
  }
  
  private def checkForDuplicates(defs:List[Def]):(List[Def], List[TypeError]) = {
      val (nonDups, dups) = checkForDuplicates(Nil, defs)
      (nonDups, dups map { d => DuplicateDefError(d.name) })
  }
  
  private def checkForDuplicates(alreadyDefined:List[Def], defs:List[Def]):(List[Def], List[Def]) =
      defs match {
        case Nil           => (alreadyDefined, Nil)
        case first :: rest => {
          if(alreadyDefined exists { _.name == first.name }) {
            val (restNonDups, restDups) = checkForDuplicates(alreadyDefined, rest)
            (restNonDups, first :: restDups)
          } else {
            checkForDuplicates(first :: alreadyDefined, rest)
          }
        }
      }
  
  private def checkForValidMain(defs:List[Def]):List[TypeError] =
      // check that main exists and is () => Null
      (defs find { _.name == "main" }) match {
        case Some(d) => (d.returnType == TypeExpr("Null",Nil) && d.params == Nil) match {
          case true  => Nil
          case false => List(InvalidMainError(mkFuncTypeExpr(d)))
        }
        case None => List(NoMainError)
      }
  
  def addTypeSigs(ft:FuncTable, defs:List[Def]):FuncTable =
      defs.foldLeft(ft){ (ft, d) => ft + (d.name -> mkFuncTypeExpr(d)) }
  
  private def mkFuncTypeExpr(d:Def):TypeExpr =
      TypeExpr("Function" + d.params.length, (d.params map { _.argType }) ::: List(d.returnType))
  
  private def checkCompliance(defs:List[Def], ft:FuncTable, tt:TypeTree):List[TypeError] =
      defs flatMap { d => checkDef(d, ft, tt) }
  
  def checkDef(d:Def, ft:FuncTable, tt:TypeTree):List[TypeError] = {
    val scope = Map() ++ (d.params map { ps => (ps.name, ps.argType) })
    (d.params flatMap { ps => tt.checkValidTypeExpr(ps.argType) }) :::
    (tt.checkValidTypeExpr(d.returnType)) :::
    (checkCalls(d.body, scope, ft, tt) match {
      case Nil => tt.complies(d.returnType, inferType(d.body, scope, ft, tt))
      case l   => l
    })
  }
  
  def checkCalls(e:Expr, s:Scope, ft:FuncTable, tt:TypeTree):List[TypeError] =
      e match {
        case a:Atom          => Nil
        case IfExpr(i, c, e) => checkCalls(List(i, c, e), s, ft, tt)
        case c:Call          => checkCall(c, s, ft, tt)
      }
    
  def checkCalls(es:List[Expr], s:Scope, ft:FuncTable, tt:TypeTree):List[TypeError] =
      es flatMap { e => checkCalls(e, s, ft, tt) }
  
  def checkCall(call:Call, scope:Scope, ft:FuncTable, tt:TypeTree):List[TypeError] = {
    val giv = call.args
    (scope get call.name) match {
      case Some(_) => Nil
      case None    => (ft get call.name) match {
        case Some(funcType) => {
          val exp = funcType.args.init
          if(exp.length == giv.length)
            Nil
          else
            WrongNumCallArgs(call.name, exp.length, giv.length) ::
                                                checkArgCompliance(exp, giv, scope, ft, tt)
        }
        case None => NonexistentFuncError(call.name) :: checkCalls(call.args, scope, ft, tt)
      }
    }
  }
  
  def checkArgCompliance(expTypes:List[TypeExpr], args:List[Expr],
                                        s:Scope, ft:FuncTable, tt:TypeTree):List[TypeError] =
      checkCalls(args.dropRight(expTypes.length - args.length), s, ft, tt) :::
      ((expTypes zip args) flatMap { case (et, e) => checkCalls(e, s, ft, tt) match {
        case Nil => tt.complies(et, inferType(e, s, ft, tt))
        case l   => l
      } })
  
  def inferType(expr:Expr):TypeExpr = // for outside use
      checkCalls(expr, Map[String,TypeExpr](), rootFuncTypes, typeTree) match {
        case Nil => inferType(expr, Map(), rootFuncTypes, typeTree)
        case es  => throw TypeErrors(es)
      }
  
  def inferType(expr:Expr, s:Scope, ft:FuncTable, tt:TypeTree):TypeExpr = {
    expr match {
      case IntLit(_)       => TypeExpr("Int",Nil)
      case CharLit(_)      => TypeExpr("Char",Nil)
      case FloatLit(_)     => TypeExpr("Float",Nil)
      case IfExpr(_, i, t) => tt.deepestCommonAncestor(inferType(i, s, ft, tt),
                                                       inferType(t, s, ft, tt))
      case c:Call          => if(isParam(c, s)) s(c.name) else ft(c.name).args.last
    }
  }
  
  private def isParam(call:Call, scope:Scope):Boolean =
      call.args.isEmpty && (scope isDefinedAt call.name)
  
}
