package vmlang.callgraph

import vmlang.common.optparser._

import vmlang.compiler._
import vmlang.compiler.typecheck._
import vmlang.compiler.ast._

object Main extends OptParser {
  
  def numArgs(n:Int) = n == 1
  val argErrorMsg = "supply 1 file to show callgraph for"
  val knownFlags = List()
  val defaultOpts = Map[String,String]()
  val help = "usage: vmlcg <file to show call graph for>"
  
  def run(args:List[String], flags:List[String], opts:Map[String,String]):Unit =
      try {
        val callTuples = extractCalls(
                            TypeCheck(
                              MakeEnv(
                                Parse(
                                  loadFile(
                                    args.head
                                  )
                                ),
                                TypeCheck.rootFuncTypes,
                                TypeCheck.typeTree
                              )
                            ).defs) flatMap { case (n, cs) => cs map { c => (n, c) } }
        callTuples foreach { case (caller, callee) => println(caller + "\t" + callee) }
      } catch {
        case e:CompilerError => println(e.repr)
      }
  
  def extractCalls(defs:Map[String,Def]):Map[String,List[String]] =
      Map() ++ (defs map { case (n,d) => (n, calls(d.body) filter {
                                    c => !d.params.exists { _.name == c } }) })
  
  def calls(e:Expr):List[String] =
      e match {
        case a:Atom => Nil
        case IfExpr(c, i, e) => calls(c) ::: calls(i) ::: calls(e)
        case Call(name, args) => name :: (args flatMap (calls _))
      }
  
}
