package vmlang.compiler

import vmlang.compiler.typecheck.TypeCheck

object Compile {
  
  def apply(prog:String):Array[Byte] = Linearize(TypeCheck(Parse(prog)))
  
}
