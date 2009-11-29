package vmlang.compiler

// intermediate code: source > intermediate code > { MiniVM, JVM, x86 ... } 

abstract class IOpcode

class Stop() extends IOpcode
class Goto(a:Int) extends IOpcode
class GotoFunc(f:String) extends IOpcode
class GotoIf(ref:IOpcode) extends IOpcode
class RetI() extends IOpcode

class PushConstI(c:Int) extends IOpcode
class PushConstC(c:Char) extends IOpcode

class AddI() extends IOpcode
class SubI() extends IOpcode
class MultI() extends IOpcode
class DivI() extends IOpcode

class CmpEq(ref:IOpcode) extends IOpcode
class CmpNeq(ref:IOpcode) extends IOpcode
class CmpGt(ref:IOpcode) extends IOpcode
class CmpGte(ref:IOpcode) extends IOpcode
class CmpLt(ref:IOpcode) extends IOpcode
class CmpLte(ref:IOpcode) extends IOpcode

class Neg() extends IOpcode
class NegLt() extends IOpcode
class And() extends IOpcode
class Or() extends IOpcode

class PrintChar() extends IOpcode
class ReadChar() extends IOpcode
