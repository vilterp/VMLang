package parse

import util.parsing._
import input._
import input.CharArrayReader.EofCh
import combinator.lexical._
import syntax._

class Lex extends Lexical with VMLangTokens {

  type Token = VMLangToken
  
  val keywords = Set("module","import","export","where","let",
                        "if","else","then","class","interface","this")
  
  def token =
    positioned(
      letter ~ rep( letter | digit ) ^^ { case first ~ rest => processIdent(first + rest.mkString) }
      | (digit *) ~ ('.' ~> (digit +)) ^^ { case intPart ~ decPart => FloatLiteral(intPart.mkString + "." + decPart.mkString) }
      | (digit +) ^^ { digits => IntLiteral(digits.mkString) }
      | '\'' ~> chrValueInCharLit <~ '\'' ^^ { c => CharLiteral(new String(Array(c))) }
      | '"' ~> (chrValueInStringLit *) <~ '"' ^^ { cs => StringLiteral(cs.mkString) }
      | EofCh ^^^ EOFToken
      | '\'' ^^ { c => ErrorTok("Unclosed char literal") }
      | '"' ^^ { c => ErrorTok("Unclosed string literal") }
      | delim
      | chrExcept() ^^ { c => ErrorTok("Illegal character: " + c) }
    )
  
  def whitespace = (whitespaceChar *) ~ (( '#' ~ (chrExcept('\n',EofCh) *) ) ?)
  
  def processIdent(s:String) =
      if(keywords contains s) Keyword(s) else Identifier(s)
  
  def chrValueInCharLit:Parser[Char] =
    ( chrExcept('\n', '\'', EofCh, '\\')
    | '\\' ~> ( 'n' ^^ { c => '\n' }
              | 't' ^^ { c => '\t' }
              | '\'' ^^ { c => '\'' }
              | 'u' ~> ( digit ~ digit ~ digit ) ^^ { case d ~ d1 ~ d2 => ("" + d + d1 + d2).toInt.toChar }
              )
    )
  
  def chrValueInStringLit:Parser[Char] =
    ( chrExcept('\n', '"', EofCh, '\\')
    | '\\' ~> ( 'n' ^^ { c => '\n' }
              | 't' ^^ { c => '\t' }
              | '"' ^^ { c => '"' }
              | 'u' ~> ( digit ~ digit ~ digit ) ^^ { case d ~ d1 ~ d2 => ("" + d + d1 + d2).toInt.toChar }
              )
    )
  
  def delim =
    ( '>' ~ '='
    | '<' ~ '='
    | '=' ~ '>'
    | '=' ~ '='
    | '!' ~ '='
    | '+'
    | '-'
    | '*'
    | '/'
    | '('
    | ')'
    | '['
    | ']'
    | '{'
    | '}'
    | '='
    | ':'
    | ','
    | '.'
    | '>'
    | '<' ) ^^ { case a ~ b => Keyword("" + a + b)
                 case a:Char => Keyword(new String(Array(a))) }
  
}
