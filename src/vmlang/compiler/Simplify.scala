package vmlang.compiler

import vmlang.compiler.ast._
import collection.immutable.HashSet

// TODO: inline calls to functions like "a:Int = 2"

object Simplify {
  
  def apply(prog:Map[String,CheckedDef]) =
      Map[String,CheckedDef]() ++
        (prog map { m => (m._1 -> CheckedDef(m._2.params, simplify(m._2.body))) })
  
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
