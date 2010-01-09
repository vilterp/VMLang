package vmlang.assembler

import util.parsing.input.CharSequenceReader
import util.parsing.combinator.JavaTokenParsers

object Parse extends JavaTokenParsers {
  
  def prog = (instr+) ^^ { is => Prog(is) }
  
  def instr = opcode ~ (arg?) ^^ { case oc ~ a => Instr(oc, a) }
  
  def opcode = ident
  
  def arg = wholeNumber ^^ { _.toInt }
  
  def apply(input:String):Prog =
      phrase(prog)(new CharSequenceReader(input)) match {
        case Success(t, _) => t
        case e:NoSuccess => throw ParserError(e.toString)
      }
  
}

case class ParserError(msg:String) extends Exception

abstract class ASTNode
case class Prog(instrs:List[Instr]) extends ASTNode
case class Instr(opcode:String, arg:Option[AnyVal])
