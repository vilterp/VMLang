package vmlang.compiler

import vmlang.compiler.ast._
import collection.immutable.HashSet

object Simplifier {
  
  val ops = HashSet("+","-","*","/",">","<",">=","<=","==","and","or","!")
  
  def apply(prog:Prog) = Prog(prog.defs map { case Def(n,a,r,body) => Def(n,a,r,simplify(body)) })
  
  def simplify(e:Expr):Expr = e match {
    case a:Atom => a
    case IfExpr(c,i,e) => IfExpr(simplify(c),simplify(i),simplify(e))
    case Call(n,args) => if (ops contains n) simplify(Call(n,args map simplify)) else Call(n,args map simplify)
  }
  
}
