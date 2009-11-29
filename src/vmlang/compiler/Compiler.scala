// package vmlang.compiler
// 
// import vmlang.compiler.ast._
// import collection.immutable.HashMap
// 
// object Compiler {
//   
//   def apply(prog:Prog) = {
//     prog.defs.foldLeft(new HashMap[String,List[IOpcode]]){
//                (map,funcDef) => (map + (funcDef.name -> compile(funcDef)))
//                    .asInstanceOf[HashMap[String,List[IOpcode]]] }
//                    // don't know why that !@$!@ "asInstanceOf" is necessary...
//   }
//   
//   def compile(funcDef:Def) = {
//     compExpr(funcDef.body)
//   }
//   
//   def compExpr(e:Expr):List[IOpcode] = e match {
//     
//     case Call("+",List(a,b)) => compExpr(a) ::: compExpr(b) ::: List(AddI())
//     case Call("-",List(a,b)) => compExpr(a) ::: compExpr(b) ::: List(SubI())
//     case Call("*",List(a,b)) => compExpr(a) ::: compExpr(b) ::: List(MultI())
//     case Call("/",List(a,b)) => compExpr(a) ::: compExpr(b) ::: List(DivI())
//     
//     case Call("==",List(a,b)) => compExpr(a) ::: compExpr(b) ::: List(CmpEq())
//     case Call("!=",List(a,b)) => compExpr(a) ::: compExpr(b) ::: List(CmpNeq())
//     case Call(">",List(a,b))  => compExpr(a) ::: compExpr(b) ::: List(CmpGt())
//     case Call(">=",List(a,b)) => compExpr(a) ::: compExpr(b) ::: List(CmpGte())
//     case Call("<",List(a,b))  => compExpr(a) ::: compExpr(b) ::: List(CmpLt())
//     case Call("<=",List(a,b)) => compExpr(a) ::: compExpr(b) ::: List(CmpLte())
//     
//     case Call("true",_) => List(PushConstI(1))
//     case Call("false",_) => List(PushConstI(0))
//     
//     case Call("printChar",List(e)) => compExpr(e) ::: List(PrintChar())
//     case Call("readChar",_) => List(ReadChar())
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
