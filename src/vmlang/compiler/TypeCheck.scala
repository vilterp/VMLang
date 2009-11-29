// package vmlang.compiler
// 
// import vmlang.compiler.ast._
// import collection.immutable.{HashMap, HashSet}
// 
// object TypeCheck(prog:Prog,) {
//   
//   // how use instance vars...!
//   
//   // val typeHierarchy = HashSet[Type](Type("Int",Nil), Type("Char",Nil), Type("Boolean",Nil))
//   // use typeTree
//   
//   def apply(prog:Prog):Prog = {
//     val returnTypes = prog.defs.foldLeft(new HashMap[String,Type]){
//                                   (m,d) => m + (d.name -> d.returnType)}
//     val argTypes = prog.defs.foldLeft(new HashMap[String,List[Type]]){
//                                   (m,d) => m + (d.name -> d.argTypes)}
//   }
//   
//   def infer(e:Expr) = e match {
//     // atoms
//     case Integer(_) => Type("Int",Nil)
//     case Char(_) => Type("Char",Nil)
//     case Call("true",_) => Type("Boolean",Nil)
//     case Call("false",_) => Type("Boolean",Nil)
//     // calls
//     case Call(n,args) => 
//   }
//   
//   def correctArgTypes()
//   
// }
