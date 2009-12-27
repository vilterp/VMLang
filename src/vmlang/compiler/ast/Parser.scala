package vmlang.compiler.ast

import scala.runtime.RichString
import scala.util.parsing.combinator.syntactical._

case class ParserError(msg:String) extends NormalCompilerError {
  val repr = "Parser Error: " + msg
}

object Parser extends StandardTokenParsers {
  
  lexical.delimiters ++= List("+","-","*","/","(",")","[","]","=","=>",":",",",">","<",
                                     ">=","<=","==","!","!=")
  lexical.reserved ++= List("if","then","else","and","or")
  
  // RULES
  
  def program = (definition *) ^^ { l => Prog(l) }
  
  def definition = ident ~ (paramsSpec?) ~ typeSpec ~ ("=" ~> expr) ^^ {
                                              case i ~ Some(ps) ~ rt ~ e => Def(i,ps,rt,e)
                                              case i ~ None ~ rt ~ e => Def(i,Nil,rt,e) }
  
  def paramsSpec = "(" ~> repsep(paramSpec, ",") <~ ")"
  
  def paramSpec = ident ~ typeSpec ^^ { case i ~ t => ParamSpec(i,t) }
  
  def typeSpec = ":" ~> typeExpr
  
  def typeExpr:Parser[TypeExpr] = ( normalTypeExpr | funcTypeExpr )
  
  def normalTypeExpr:Parser[TypeExpr] = ident ~ (typeParams?) ^^ { case i ~ Some(tp) => TypeExpr(i,tp)
                                                                   case i ~ None     => TypeExpr(i,Nil) }
  
  def funcTypeExpr:Parser[TypeExpr] = ("(" ~> repsep(typeExpr,",") <~ ")") ~ ("=>" ~> typeExpr) ^^ {
                                        case pts ~ rt => if(pts.length <= 8)
                                                            TypeExpr("Function" + pts.length, pts ::: List(rt))
                                                          else
                                          throw ParserError("can't make a function with more than 8 parameters") }
  
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
                      "!=" ^^^ { (a:Expr, b:Expr) => Call("!=",List(a,b)) } )
                      // I'm sure there's a super-elegant way to do this,
                        // but my Scala chops aren't up to it yet
  
  def sum = product * (
                      "+" ^^^ { (a:Expr, b:Expr) => Call("+",List(a,b)) } |
                      "-" ^^^ { (a:Expr, b:Expr) => Call("-",List(a,b)) } )
  
  def product = atom * (
                      "*" ^^^ { (a:Expr, b:Expr) => Call("*",List(a,b)) } |
                      "/" ^^^ { (a:Expr, b:Expr) => Call("/",List(a,b)) } )
  
  def atom = ( int | list | string | call | parenthesizedExpr | unaryMinus | unaryNot )
            // todo: char lit (not in standardtokenparsers...)
  
  def list = "[" ~> repsep(expr,",") <~ "]" ^^ concatIze
  
  def string = stringLit ^^ { (s:String) => concatIze(new RichString(s) map { CharLit(_) }) }
  
  def concatIze(items:Seq[Expr]):Expr = items.foldRight(EmptyList.asInstanceOf[Expr]){
                                                              (i,a) => Call(":",List(i,a)) }
  
  def parenthesizedExpr = "(" ~> expr <~ ")"
  
  def unaryNot:Parser[Expr] = "!" ~> atom ^^ { a => Call("!",List(a)) }
  
  def unaryMinus:Parser[Expr] = "-" ~> atom ^^ { a => Call("*",List(a,IntLit(-1))) }
  
  def int = numericLit ^^ { s => IntLit(s.toInt) }
  
  def call = ident ~ (args ?) ^^ { case i ~ Some(a) => Call(i,a)
                                   case i ~ None => Call(i,Nil) }
  
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
        throw ParserError(e.toString)
    }
  }
  
}
