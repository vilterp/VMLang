package vmlang.compiler

// intermediate code: source > intermediate code > { MiniVM, JVM, x86 ... } 

abstract class IOpcode

case object Stop extends IOpcode
case class Goto(a:Int) extends IOpcode
case class GotoFunc(f:String) extends IOpcode
case class GotoIf(ref:IOpcode) extends IOpcode
case object RetI extends IOpcode

case class PushConstI(c:Int) extends IOpcode
case class PushConstC(c:Char) extends IOpcode

case object AddI extends IOpcode
case object SubI extends IOpcode
case object MultI extends IOpcode
case object DivI extends IOpcode

case class CmpEq(ref:IOpcode) extends IOpcode
case class CmpNeq(ref:IOpcode) extends IOpcode
case class CmpGt(ref:IOpcode) extends IOpcode
case class CmpGte(ref:IOpcode) extends IOpcode
case class CmpLt(ref:IOpcode) extends IOpcode
case class CmpLte(ref:IOpcode) extends IOpcode

case object Neg extends IOpcode
case object NegLt extends IOpcode
case object And extends IOpcode
case object Or extends IOpcode

case object PrintChar extends IOpcode
case object ReadChar extends IOpcode
