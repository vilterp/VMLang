// package vmlang.compiler
// 
// import vmlang.common.optparser._
// 
// import vmlang.compiler.typecheck.TypeCheck
// import vmlang.compiler.ast._
// import vmlang.vm.{VM, VMError}
// 
// import java.util.Scanner
// 
// import collection.mutable.HashMap
// 
// object Main extends OptParser {
//   
//   def numArgs(n:Int) = n == 0 || n == 1
//   val argErrorMsg = "supply 1 file to compile, or no argument for interactive prompt"
//   val knownFlags = List("v")
//   val defaultOpts = Map[String,String]()
//   val help = "usage: vmlc <optional source file> <options>\n" +
//              "-v     print out messages about what the compiler is doing"
//   
//   def run(args:List[String], flags:List[String], opts:Map[String,String]):Unit =
//       args match {
//         case Nil       => runIPrompt
//         case List(fn)  =>
//             try {
//               writeFile(Compile(loadFile(fn)), (fn split '.')(0) + ".vmlc")
//             } catch {
//               case e:CompilerError => throw FatalError(e.repr)
//             }
//       }
//   
//   def runIPrompt = {
//     val s = new Scanner(System.in)
//     var counter = 0
//     var defs = Map[String,Def]()
//     while(true) {
//       print(">> ")
//       try {
//         val input = s.nextLine
//         if(input startsWith ":t ") {
//           println(TypeCheck.inferType(Parse.parseExpr(input substring 3)))
//         } else {
//           Parse.parseIPromptStmt(input) match {
//             case e:Expr =>
//                 defs = defs + ("main" -> Def("main", Nil, TypeExpr("Null",Nil), e match {
//                   case Call("printInt",args) => e
//                   case expr                  => Call("printInt",List(expr))
//                 }))
//             case d:Def  =>
//                 defs = defs + (d.name -> d)
//           }
//           val code = Linearize(Simplify(TypeCheck(Prog(defs))))
//           new VM(code, 1024, 1024).run
//         }
//       } catch {
//         case e:CompilerError => println(e.repr)
//         case e:VMError       => println(e.getMessage)
//       }
//     }
//   }
//   
// }
