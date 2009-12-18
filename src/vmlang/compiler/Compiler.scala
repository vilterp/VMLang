// package vmlang.compiler
// 
// import vmlang.compiler.ast._
// import vmlang.compiler.model._
// import vmlang.compiler.icode._ // group these imports into one?
// import collection.immutable.Map
// 
// object Compiler {
//   
//   def apply(funcTable:Map[String,Function]):List[(String,List[IOpcode])] = {
//     (funcTable("main") match {
//       case Some(f) => ("main",compFunc(f))
//       case None => throw new NonexistentFuncError("main","[compiler]")
//     }) ::: ((funcTable - "main") map { p => (p._1,compFunc(p._2)) } )
//   }
//   
//   def compFunc(f:Function):List[IOpcode] = f match {
//     case PrimFunc(code) => code
//     case NormFunc(expr) => compExpr(expr)
//   }
//   
//   def compExpr(e:Expr):List[IOpcode] = e match {
//     
//     // TODO: time complexity of ":::" -- are all these calls gonna kill us?
//     
//     case Call("+",List(a,b)) => compExpr(a) ::: compExpr(b) ::: List(AddI)
//     case Call("-",List(a,b)) => compExpr(a) ::: compExpr(b) ::: List(SubI)
//     case Call("*",List(a,b)) => compExpr(a) ::: compExpr(b) ::: List(MultI)
//     case Call("/",List(a,b)) => compExpr(a) ::: compExpr(b) ::: List(DivI)
//     
//     case Call("==",List(a,b)) => compExpr(a) ::: compExpr(b) ::: List(CmpEq)
//     case Call("!=",List(a,b)) => compExpr(a) ::: compExpr(b) ::: List(CmpNeq)
//     case Call(">",List(a,b))  => compExpr(a) ::: compExpr(b) ::: List(CmpGt)
//     case Call(">=",List(a,b)) => compExpr(a) ::: compExpr(b) ::: List(CmpGte)
//     case Call("<",List(a,b))  => compExpr(a) ::: compExpr(b) ::: List(CmpLt)
//     case Call("<=",List(a,b)) => compExpr(a) ::: compExpr(b) ::: List(CmpLte)
//     
//     case Call("true",_) => List(PushConstI(1))
//     case Call("false",_) => List(PushConstI(0))
//     
//     case Call("printChar",List(e)) => compExpr(e) ::: List(PrintChar)
//     case Call("readChar",_) => List(ReadChar)
//     
//     case Call(name,args) => (args flatMap comExpr) ::: List()
//     
//     case Integer(v) => List(PushConstI(v))
//     case Char(c) => List(PushConstC(c))
//     
//     case IfExpr(c,i,e) => { val el = compExpr(e)
//                             compExpr(c) ::: List(GotoIf(el.head)) ::: compExpr(i) ::: el }
//     
//   }
//   
// }
