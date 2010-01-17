package vmlang.compiler

import vmlang.common.optparser._

import vmlang.compiler.typecheck.TypeCheck
import vmlang.compiler.ast._
import vmlang.vm.{VM, VMError}

import java.util.Scanner

import collection.mutable.HashMap

object Main extends OptParser {
  
  def numArgs(n:Int) = n == 0 || n == 1
  val argErrorMsg = "supply 1 file to compile, or no argument for interactive prompt"
  val knownFlags = List("v")
  val defaultOpts = Map[String,String]()
  val help = "usage: vmlc <optional source file> <options>\n" +
             "-v     print out messages about what the compiler is doing"
  
  def run(args:List[String], flags:List[String], opts:Map[String,String]):Unit =
      args match {
        case Nil       => runREPL
        case List(fn)  =>
            try {
              writeFile(Compile(loadFile(fn)), ( fn split '.')(0) + ".vmlc")
            } catch {
              case e:CompilerError => throw FatalError(e.repr)
            }
      }
  
  def runREPL = {
    val s = new Scanner(System.in)
    var resCounter = 0
    var env = Env(Map[String,Def](), TypeCheck.rootFuncTypes, TypeCheck.typeTree)
    while(true) {
      print(">> ")
      try {
        val input = s.nextLine
        if(input startsWith ":t ") {
          println(TypeCheck.inferType(Parse.parseExpr(input substring 3), env))
        } else {
          Parse.parseREPLStmt(input) match {
            case e:Expr =>
                env = env.addDef(Def("res" + resCounter, Nil, TypeCheck.inferType(e, env), e))
                env = env.addDef(Def("main", Nil, TypeExpr("Null", Nil),
                                      Call("printInt",List(Call("res" + resCounter, Nil)))))
                resCounter += 1
                val code = Linearize(Simplify(TypeCheck(env)))
                new VM(code, 1024, 1024).run
            case d:Def  =>
                env = env.addDef(d)
          }
        }
      } catch {
        case e:CompilerError => println(e.repr)
        case e:VMError       => println(e.getMessage)
      }
    }
  }
  
}
