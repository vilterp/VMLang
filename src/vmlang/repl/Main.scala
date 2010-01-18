package vmlang.repl

import java.util.Scanner

import vmlang.common.optparser._

import vmlang.compiler._
import vmlang.compiler.typecheck._
import vmlang.compiler.ast._

import vmlang.vm.{ VM, VMError }

object Main extends OptParser {
  
  def numArgs(n:Int) = n == 0
  val argErrorMsg = "run with no args for interactive prompt"
  val knownFlags = List("h")
  val defaultOpts = Map[String,String]()
  val help =  "usage: vmli"
  
  def run(args:List[String], flags:List[String], opts:Map[String,String]):Unit = {
    flags match {
      case "h" :: fs => println(argErrorMsg)
      case Nil       => runREPL
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
                print("res" + resCounter + ": ")                      
                resCounter += 1
                val code = Linearize(TypeCheck(env))
                new VM(code, 1024, 1024, false).run
            case d:Def  =>
                val newEnv = env.addDef(d)
                env = TypeCheck.checkDef(d, newEnv) match {
                  case Nil => newEnv
                  case es  => throw TypeErrors(es.removeDuplicates)
                }
          }
        }
      } catch {
        case e:CompilerError => println(e.repr)
        case e:VMError       => println(e.getMessage)
      }
    }
  }
  
}
