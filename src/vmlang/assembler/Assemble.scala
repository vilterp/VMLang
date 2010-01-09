package vmlang.assembler

import vmlang.common.Opcodes._
import vmlang.common.Opcodes

case class AssemblyError(msg:String) extends Exception

object Assemble {
  
  val opcodes = Map[String,Byte]() ++ (Opcodes.values map { oc => (oc.toString, oc.toByte) })
  
  def apply(prog:Prog):List[Byte] =
      prog.instrs flatMap { case Instr(opcode, arg) => (opcodes get opcode) match {
        case Some(byte) => opcode match {
          case "I_CONST_A" |
               "I_CONST_B" |
               "GOTO" => byte :: (arg match {
                 case Some(i) => i match {
                   case i:Int => intBytes(i)
                   case _     => throw AssemblyError("opcode " + opcode + " takes an int argument")
                 }
                 case None    => throw AssemblyError("opcode " + opcode + " takes an int argument")
               })
          case "B_CONST_A" |
               "B_CONST_B" => byte :: (arg match {
                  case Some(b) => b match {
                    case b:Byte => b :: Nil
                    case _      => throw AssemblyError("opcode " + opcode + " takes a byte argument")
                  }
                  case None    => throw AssemblyError("opcode " + opcode + " takes a byte argument")
               })
          case _ => List(byte)
        }
        case None => throw AssemblyError("nonexistent opcode: " + opcode)
      } }
  
  def intBytes(i:Int):List[Byte] =
    (0xff & (i << 24)).asInstanceOf[Byte] ::
    (0xff & (i << 16)).asInstanceOf[Byte] ::
    (0xff & (i << 8)).asInstanceOf[Byte] ::
    (0xff & i).asInstanceOf[Byte] :: Nil
  
}
