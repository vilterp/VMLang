// package vmlang.compiler
// 
// import vmlang.compiler.ast._
// import collection.immutable.HashSet
// 
// object Simplifier {
//   
//   def apply(prog:Prog) = Prog(prog.defs map { case Def(n,a,r,body) => Def(n,a,r,simplify(body)) })
//   
//   def simplify(e:Expr):Expr = e match {
//     case a:Atom => a
//     case IfExpr(c,i,e) => (simplify(c), simplify(i), simplify(e)) match {
//       case (Call("true",Nil), i, e) => i
//       case (Call("false",Nil), i, e) => i
//       case (c, i, e) => IfExpr(c, i, e)
//     }
//     case Call(n,args) => (n, args map simplify) match {
//       case ("+" , List(Integer(a),Integer(b))) => Integer(a + b)
//       case ("-" , List(Integer(a),Integer(b))) => Integer(a - b)
//       case ("*" , List(Integer(a),Integer(b))) => Integer(a * b)
//       case ("/" , List(Integer(a),Integer(b))) => Integer(a / b)
//       case (">" , List(Integer(a),Integer(b))) => Integer(a  b)
//       case (">=", List(Integer(a),Integer(b))) => Integer(a + b)
//       case ("<" , List(Integer(a),Integer(b))) => Integer(a + b)
//       case ("<=", List(Integer(a),Integer(b))) => Integer(a + b)
//       case ("==", List(Integer(a),Integer(b))) => Integer(a + b)
//     }
//   }
//   
// }
