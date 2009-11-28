package vmlang.compiler.icode

// intermediate code: source > intermediate code > { MiniVM, JVM, x86 ... } 

abstract class IOpcode

case class PushConstant(v:Int) extends IOpcode