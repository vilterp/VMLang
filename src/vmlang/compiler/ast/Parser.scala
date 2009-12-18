package vmlang.compiler.ast

import scala.runtime.RichString
import scala.util.parsing.combinator.syntactical._

class ParserError(msg:String) extends NormalCompilerError {
  val repr = "Parser Error: " + msg
}

object Parser extends StandardTokenParsers {
  
  lexical.delimiters ++= List("+","-","*","/","(",")","[","]","=",":",",",">","<",
                                     ">=","<=","==","!","!=")
  lexical.reserved ++= List("if","then","else","and","or")
  
  // RULES
  
  def program = (definition *) ^^ { l => Prog(l) }
  
  def definition = ident ~ (argsSpec?) ~ (typeSpec?) ~ ("=" ~> expr) ^^ {
                                              case i ~ Some(as) ~ rt ~ e => Def(i,as,rt,e)
                                              case i ~ None ~ rt ~ e => Def(i,Nil,rt,e) }
  
  def argsSpec = "(" ~> repsep(argSpec, ",") <~ ")"
  
  def argSpec = ident ~ typeSpec ^^ { case i ~ t => ArgSpec(i,t) }
  
  def typeSpec = ":" ~> typeExpr
  
  def typeExpr:Parser[TypeExpr] = ident ^^ { i => TypeExpr(i) }
  
  def typeParams = "[" ~> repsep(typeExpr,",") <~ "]"
  
  def expr:Parser[Expr] = ( ifStatement | orExpr )
  
  def ifStatement = (("if" ~> orExpr) ~ ("then" ~> orExpr) ~ ("else" ~> orExpr)) ^^ {
       case c ~ i ~ e => IfExpr(c,i,e) } 
    
  def orExpr = andExpr * ( "or" ^^^ { (a:Expr, b:Expr) => Call("or",List(a,b)) } )
  
  def andExpr = comparison * ( "and" ^^^ { (a:Expr, b:Expr) => Call("and",List(a,b)) } )
  
  def comparison = sum * (
                      ">" ^^^ { (a:Expr, b:Expr) => Call(">",List(a,b)) } |
                      "<" ^^^ { (a:Expr, b:Expr) => Call("<",List(a,b)) } |
                      "==" ^^^ { (a:Expr, b:Expr) => Call("==",List(a,b)) } |
                      "<=" ^^^ { (a:Expr, b:Expr) => Call("<=",List(a,b)) } |
                      ">=" ^^^ { (a:Expr, b:Expr) => Call(">=",List(a,b)) } |
                      "!=" ^^^ { (a:Expr, b:Expr) => Call("!",List(a,b)) } )
                      // I'm sure there's a super-elegant way to do this,
                        // but my Scala chops aren't up to it yet
  
  def sum = product * (
                      "+" ^^^ { (a:Expr, b:Expr) => Call("+",List(a,b)) } |
                      "-" ^^^ { (a:Expr, b:Expr) => Call("-",List(a,b)) } )
  
  def product = atom * (
                      "*" ^^^ { (a:Expr, b:Expr) => Call("*",List(a,b)) } |
                      "/" ^^^ { (a:Expr, b:Expr) => Call("/",List(a,b)) } )
  
  def atom = ( number | list | string | call | parenthesizedExpr | unaryMinus | unaryNot )
            // todo: char lit (not in standardtokenparsers...)
  
  def list = "[" ~> repsep(expr,",") <~ "]" ^^ concatIze
  
  def string = stringLit ^^ { (s:String) => concatIze(new RichString(s) map { CharLit(_) }) }
  
  def concatIze(items:Seq[Expr]):Expr = items.foldRight(EmptyList.asInstanceOf[Expr]){
                                                              (i,a) => Call(":",List(i,a)) }
  
  def parenthesizedExpr = "(" ~> expr <~ ")"
  
  def unaryNot:Parser[Expr] = "!" ~> atom ^^ { a => Call("!",List(a)) }
  
  def unaryMinus:Parser[Expr] = "-" ~> atom ^^ { a => Call("*",List(a,IntLit(BigInt(-1)))) }
  
  def number = numericLit ^^ { s => IntLit(BigInt(s)) }
  
  def call = ident ~ (args ?) ^^ { case i ~ Some(a) => Call(i,a)
                    case i ~ None => Call(i,List()) }
  
  def args = "(" ~> repsep(expr,",") <~ ")"
  
  // END OF RULES
  
  def parse(s:String) = phrase(program)(new lexical.Scanner(s))
  
  def parseTypeExpr(t:String) = phrase(typeExpr)(new lexical.Scanner(t)) match {
    case Success(t, _) => t
    case e: NoSuccess =>
      throw new IllegalArgumentException(e.toString)
  }
  
  def apply(s:String) = {
    parse(s) match {
      case Success(tree, _) => tree
      case e: NoSuccess =>
        throw new ParserError(e.toString)
    }
  }
  
}
