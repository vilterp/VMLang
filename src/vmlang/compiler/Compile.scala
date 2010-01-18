package vmlang.compiler

import vmlang.compiler.typecheck.TypeCheck

object Compile {
  
  def apply(prog:String):Array[Byte] =
      Linearize(
//        Simplify(
          TypeCheck(
            MakeEnv(
              Parse(prog),
              TypeCheck.rootFuncTypes,
              TypeCheck.typeTree
            )
          )
//        )
      )
  
}
