package parse

import util.parsing.syntax.Tokens
import util.parsing.input.Positional

trait VMLangTokens extends Tokens {
  
  abstract class VMLangToken extends Token with Positional {
    var filePath:String = null
    def setFilePath(fp:String) = {
      filePath = fp
      this
    }
  }
  case class Identifier(chars:String) extends VMLangToken
  case class Keyword(chars:String) extends VMLangToken
  case class IntLiteral(chars:String) extends VMLangToken
  case class FloatLiteral(chars:String) extends VMLangToken
  case class CharLiteral(chars:String) extends VMLangToken
  case class StringLiteral(chars:String) extends VMLangToken
  case object EOFToken extends VMLangToken { def chars = "<EOF>" }
  case class ErrorTok(msg:String) extends VMLangToken { def chars = msg }
  
}
