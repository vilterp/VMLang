package vmlang.compiler

import vmlang.compiler.ast._
import collection.immutable.HashSet

object Simplifier {
  
  def apply(prog:Prog) = Prog(prog.defs map { case Def(n,a,r,body) => Def(n,a,r,simplify(body)) })
  
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
                                                  
    }
  }
  
}
