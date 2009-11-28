package vmlang.compiler

import vmlang.compiler.ast._
import vmlang.compiler.icode._
import collection.immutable.HashMap

object Compiler {
  
  def apply(prog:Prog) = {
    prog.defs.foldLeft(new HashMap[String,List[IOpcode]]){
               (map,funcDef) => (map + (funcDef.name -> compile(funcDef)))
                   .asInstanceOf[HashMap[String,List[IOpcode]]] }
                   // don't know why that !@$!@ "asInstanceOf" is necessary...
  }
  
  def compile(funcDef:Def) = {
    compileExpr(funcDef.body)
  }
  
}
