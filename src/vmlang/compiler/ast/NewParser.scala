import scala.util.parsing.combinator._
import scala.util.parsing.input._

// Bug: CharSequenceReader eliminates spaces in strings ("hello there" => StringLit(hellothere))

object Test extends RegexParsers {
  
  def apply(input:String) =
      doRule(expr,input) match {
        case Success(t, _) => t
        case e: NoSuccess => throw new IllegalArgumentException(e.toString)
      }
  
  def doRule(rule:Parser[Any], input:String) = phrase(rule)(new CharSequenceReader(input))
  
  def expr = ( float | int | variable | string | char )
  
  def float = regex("""-?(\d+\.\d+|\.\d+)""".r) ~ (sciNot?) ^^ {
                  case f ~ Some(s) => FloatLit(checkFloatSize(f,s))
                  case f ~ None    => FloatLit(checkFloatSize(f,0)) }
  
  def int = regex("""-?\d+""".r) ~ (sciNot?) ^^ {
                  case i ~ Some(s) => IntLit(checkIntSize(i,s))
                  case i ~ None    => IntLit(checkIntSize(i,0)) }
  
  def checkIntSize(v:String, e:Int):Int = {
    val bi = BigInt(v) * (BigInt(10) pow e)
    if(bi > Math.MAX_INT)
      throw new IllegalArgumentException("int literal too big")
    else if(bi < Math.MIN_INT)
      throw new IllegalArgumentException("int literal too small")
    else
      bi.intValue
  }
  
  def checkFloatSize(v:String, e:Int):Float = {
    val bd = BigDecimal(v) * new BigDecimal(new java.math.BigDecimal(10) pow e)
    if(bd > BigDecimal(Math.MAX_FLOAT))
      throw new IllegalArgumentException("float literal too big")
    else if(bd < BigDecimal(Math.MIN_FLOAT))
      throw new IllegalArgumentException("float literal too small")
    else
      bd.floatValue
  }
  
  def sciNot = regex("""[eE]-?\d+""".r) ^^ {
                  try {
                    i => i.substring(1).toInt
                  } catch {
                    case c:NumberFormatException => throw new IllegalArgumentException("exponent too big")
                  } }
  
  def string = "\"" ~> (stringChar*) <~ "\"" ^^ { l => StringLit(l.mkString) } // should actually map to list of char...
  
  def stringChar = ( unicodeChar | escapedCharInStr | normalCharInStr )
  
  def char = "'" ~> ( unicodeChar | escapedCharInChar | normalCharInChar ) <~ "'" ^^ { c => CharLit(c) }
  
  def normalCharInChar = regex("""[ -&\(-~]""".r) ^^ { s => s charAt 0 }
  
  def escapedCharInChar = regex("""\\[nt']""".r) ^^ { case "\\n"  => '\n' 
                                                      case "\\t"  => '\t'
                                                      case "\\'" => '\'' }
  
  def normalCharInStr = regex("""[ -!#-~]""".r) ^^ { s => s charAt 0 }
  
  def escapedCharInStr = regex("""\\[nt\"]""".r) ^^ { case "\\n"  => '\n' 
                                                      case "\\t"  => '\t'
                                                      case "\\\"" => '"' }
  
  def unicodeChar = regex("""\\u[0-9a-f]{1,4}""".r) ^^ { s => Integer.parseInt(s substring 2,16).toChar }
  
  def variable = regex("""[a-zA-Z_]\w*""".r) ^^ { i => Var(i) }
  
  def main(args:Array[String]) = {
    List("123","-123","123.1234","123e4","hello","hello_o","HelloYo","_yo",
        "64654654654654645564654456564654654654654","5e65465465465465465","-.123","000.3","8e4","Infinity",
        "'a'","'!'","'\\n'","'\\u61'","'\\''",
        "\"Sup?\"","\"hey\\nthere\"","\"'that is cool'\"","\"\\\"\"") foreach test
  }
  
  def test(input:String) = println(input + " => " + {
    try {
      apply(input)
    } catch {
      case e: IllegalArgumentException => "error: " + e.getMessage
    }
  })
  
}

abstract class Atom
case class CharLit(value:Char) extends Atom
case class FloatLit(value:Float) extends Atom
case class IntLit(value:Int) extends Atom
case class StringLit(value:String) extends Atom
case class Var(name:String) extends Atom
