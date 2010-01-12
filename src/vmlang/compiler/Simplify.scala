package vmlang.compiler

import vmlang.compiler.ast._

import collection.Set

object Simplify {
  
  type DefMap = Map[String,Def]
  type FuncTable = Map[String,TypeExpr]
  
  def apply(env:Env):Env =
      Env(inlineAll(simplifyAll(env.defs), Set() ++ env.ft.keySet.toList -- env.defs.keySet.toList),
        env.ft, env.tt)
  
  def simplifyAll(defs:DefMap) =
      applyToAllDefs(defs, simplify _)
  
  def inlineAll(defs:DefMap, rootNames:Set[String]) =
      Map[String,Def]() ++
        (applyToAllDefs(defs, { e => inline(e,defs,rootNames) }) filter {
          case (_, d) => !d.body.isInstanceOf[Atom] })
  
  def applyToAllDefs(defs:DefMap, f:(Expr) => Expr):DefMap =
      Map[String,Def]() ++ (defs map { case (na, Def(n, ps, rt, b)) => (na, Def(n, ps, rt, f(b))) })
  
  def inline(e:Expr, defs:DefMap, rootNames:Set[String]):Expr =
      e match {
        case a:Atom => a
        case IfExpr(c, i, e) =>
            IfExpr(inline(c, defs, rootNames), inline(c, defs, rootNames), inline(c, defs, rootNames))
        case Call(name, args) =>
          if(rootNames contains name)
            Call(name, args map { e => inline(e, defs, rootNames) })
          else
            defs(name).body match {
              case a:Atom => a
              case _      => Call(name, args map { e => inline(e, defs, rootNames) })
            }
      }
  
  def simplify(e:Expr):Expr = e match {
    case a:Atom => a
    case IfExpr(c,i,e) => (simplify(c), simplify(i), simplify(e)) match {
      case (Call("true",Nil), i, e) => i
      case (Call("false",Nil), i, e) => e
      case (c, i, e) => IfExpr(c, i, e)
    }
    case Call(n,args) => (n, args map simplify) match {
      
      case ("+" , List(IntLit(a),IntLit(b)))      => IntLit(a + b)
      case ("-" , List(IntLit(a),IntLit(b)))      => IntLit(a - b)
      case ("*" , List(IntLit(a),IntLit(b)))      => IntLit(a * b)
      case ("/" , List(IntLit(a),IntLit(b)))      => IntLit(a / b)
                                                  
      case (">" , List(IntLit(a),IntLit(b)))      => Call(if(a > b) "true" else "false", Nil)
      case (">=", List(IntLit(a),IntLit(b)))      => Call(if(a >= b) "true" else "false", Nil)
      case ("<" , List(IntLit(a),IntLit(b)))      => Call(if(a < b) "true" else "false", Nil)
      case ("<=", List(IntLit(a),IntLit(b)))      => Call(if(a <= b) "true" else "false", Nil)
      case ("==", List(IntLit(a),IntLit(b)))      => Call(if(a == b) "true" else "false", Nil)
                                                  
      case ("not", List(Call("true",Nil)))        => Call("false", Nil)
      case ("not", List(Call("false",Nil)))       => Call("true", Nil)
                                                  
      case ("and", List(Call(a,Nil),Call(b,Nil))) =>
                                      Call(if(a == "true" && b == "true") "true" else "false", Nil)
      case ("or", List(Call(a,Nil),Call(b,Nil)))  =>
                                      Call(if(a == "true" || b == "true") "true" else "false", Nil)
      
      case (n, args) => Call(n, args)
      
    }
  }
  
}
