package vmlang.compiler.ast

import scala.util.parsing.combinator.syntactical._

object Parser extends StandardTokenParsers {
  
  lexical.delimiters ++= List("+","-","*","/","(",")","[","]","=",":",",",">","<",
                                     ">=","<=","==","!","!=")
  lexical.reserved ++= List("if","then","else","and","or")
  
  // RULES
  
  def program = (definition *) ^^ { l => Prog(l) }
  
  def definition = ident ~ (argsSpec?) ~ (typeSpec?) ~ ("=" ~> expr) ^^ {
                                              case i ~ as ~ rt ~ e => Def(i,as,rt,e) }
  
  def argsSpec = "(" ~> repsep(argSpec, ",") <~ ")"
  
  def argSpec = ident ~ typeSpec ^^ { case i ~ t => ArgSpec(i,t) }
  
  def typeSpec = ":" ~> typeExpr
  
  def typeExpr:Parser[Type] = ident ~ (typeParams?) ^^ { case i ~ t => Type(i,t) }
  
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
                      "!=" ^^^ { (a:Expr, b:Expr) => Call("!",List(Call("==",List(a,b)))) } )
                      // I'm sure there's a super-elegant way to do this,
                        // but my Scala chops aren't up to it yet
  
  def sum = product * (
                      "+" ^^^ { (a:Expr, b:Expr) => Call("+",List(a,b)) } |
                      "-" ^^^ { (a:Expr, b:Expr) => Call("-",List(a,b)) } )
  
  def product = atom * (
                      "*" ^^^ { (a:Expr, b:Expr) => Call("*",List(a,b)) } |
                      "/" ^^^ { (a:Expr, b:Expr) => Call("/",List(a,b)) } )
  
  def atom = ( number | list | call | parenthesizedExpr | unaryMinus | unaryNot )
  
  def list = "[" ~> repsep(expr,",") <~ "]" ^^ { l => Call(":",List(EmptyList)) }
                // this should transform [1,2,3] into :(1,:(2,:(3,[])))
  
  def parenthesizedExpr = "(" ~> expr <~ ")"
  
  def unaryNot:Parser[Expr] = "!" ~> atom ^^ { a => Call("!",List(a)) }
  
  def unaryMinus:Parser[Expr] = "-" ~> atom ^^ { a => Call("*",List(a,Integer("-1"))) }
  
  def number = numericLit ^^ { s => Integer(s) }
  
  def call = ident ~ (args ?) ^^ { case i ~ Some(a) => Call(i,a)
                    case i ~ None => Call(i,List()) }
  
  def args = "(" ~> repsep(expr,",") <~ ")"
  
  // END OF RULES
  
  def parse(s:String) = phrase(program)(new lexical.Scanner(s))
  
  def apply(s:String) = {
    parse(s) match {
      case Success(tree, _) => tree
      case e: NoSuccess =>
        throw new IllegalArgumentException(e.toString)
    }
  }
  
}
