package parse

import util.parsing._
import combinator.Parsers
import input.Position

// TODO: enable defining operators (which will be lexed as Keywords, not Identifiers)
// TODO: make "module ..." optional, "Main" default

case class ParseError(msg:String, pos:Position) extends Exception

object Parse extends Parsers with VMLangTokens {
  
  // interface
  
  val l = new Lex
  
  def apply(s:String) =
      phrase(module)(new l.Scanner(s).asInstanceOf[Input]) match {
        case Success(t, _) => t
        case e:NoSuccess => throw ParserError(e.msg, e.next.first.pos)
      }
  
  // rules
  
  type Elem = VMLangToken
  
  def module = ("module" ~> qident) ~ opt(exportSpec) ~ opt(importSpec) ~ ("where" ~> rep(someDef)) ^^ { case n ~ es ~ is ~ ds => {
    val exports = es match {
      case Some(exs) => exs
      case None      => Nil
    }
    val imports = is match {
      case Some(imps) => imps
      case None => Nil
    }
    ModuleDef(n, exports, imports, ds)
  } }
  
  def exportSpec = "(" ~> rep1sep(ident, ",") <~ ")"
  
  def importSpec = "(" ~> rep1sep(imp, ",") <~ ")"
  
  def imp = qident ~ opt("." ~ "*") ^^ { case id ~ Some(all) => Import(id, true)
                                         case id ~ None      => Import(id, false) }
  
  def someDef = interfaceDef | classDef | funcDef
  
  val defaultParent = QIdent(List(Ident("builtin",CompilerPosition),Ident("Value",CompilerPosition)))
  
  def interfaceDef = ("interface" ~> ident) ~ opt(parentSpec) ~ opt(interfaceBody) ^^ { case i ~ ps ~ ib => {
    val parent = ps match {
      case Some(par) => par
      case None      => defaultParent
    }
    val body = ib match {
      case Some(defs) => defs
      case None       => Nil
    }
    InterfaceDef(i, parent, body)
  } }
  
  def interfaceBody = "{" ~> rep( absMethodDef | funcDef ) <~ "}"
  
  def absMethodDef = ident ~ opt(paramTypeSpecs) ~ typeSpec ^^ { case n ~ Some(ps) ~ rt => AbsMethodDef(n, ps,  rt)
                                                                 case n ~ None     ~ rt => AbsMethodDef(n, Nil, rt) }
  
  def paramTypeSpecs = "(" ~> repsep(typeExpr, ",") <~ ")"
  
  def classDef = ("class" ~> ident) ~ opt(paramSpecs) ~ opt(parentSpec) ~ opt(classBody) ^^ { case i ~ ps ~ pa ~ cb => {
    val params = ps match {
      case Some(pars) => pars
      case None       => Nil
    }
    val parent = pa match {
      case Some(par) => par
      case None      => defaultParent
    }
    val body = cb match {
      case Some(defs) => defs
      case None       => Nil
    }
    ClassDef(i, params, parent, body)
  } }
  
  def parentSpec = "<" ~> qident
  
  def classBody = "{" ~> rep(funcDef) <~ "}"
  
  def funcDef = ((ident ~ opt(paramSpecs) ~ typeSpec) <~ "=") ~ expr ^^ { case n ~ Some(ps) ~ rt ~ b => FuncDef(n, ps,  rt, b)
                                                                          case n ~ None     ~ rt ~ b => FuncDef(n, Nil, rt, b) }
  
  def paramSpecs = "(" ~> repsep(paramSpec, ",") <~ ")"
  
  def paramSpec = ident ~ typeSpec ^^ { case i ~ t => ParamSpec(i, t) }
  
  def typeSpec = ":" ~> typeExpr
  
  def typeExpr = qident ^^ { qi => TypeExpr(qi) }
  
  def expr:Parser[Expr] = block | letExpr | notResult
  
  def letExpr = "let" ~ rep1(binding) ~ ("in" ~> expr) ^^ { case let ~ bs ~ e => LetExpr(bs, e, let.pos) }
  
  def binding = ident ~ ("=" ~> expr) ^^ { case i ~ e => (i, e) }
  
  def block = "{" ~ rep1(expr) <~ "}" ^^ { case cb ~ es => Block(es, cb.pos) }
  
  def notResult:Parser[Expr] = opt("not") ~ orResult ^^ { case Some(op) ~ e => MethodCall(e, Ident(op.chars, op.pos), Nil)
                                                          case None     ~ e => e}
  
  def orResult:Parser[Expr] = andResult ~ opt("or" ~ powResult) ^^ { case l ~ Some(op ~ r) => MethodCall(l, Ident(op.chars, op.pos), List(r))
                                                                     case l ~ None => l }
  
  def andResult:Parser[Expr] = powResult ~ opt("and" ~ andResult) ^^ { case l ~ Some(op ~ r) => MethodCall(l, Ident(op.chars, op.pos), List(r))
                                                                       case l ~ None => l }
  
  def powResult:Parser[Expr] = addResult ~ opt("^" ~ powResult) ^^ { case l ~ Some(op ~ r) => MethodCall(l, Ident(op.chars, op.pos), List(r))
                                                                     case l ~ None => l }
  
  def addResult:Parser[Expr] = multResult ~ opt(("*" | "/") ~ addResult) ^^ { case l ~ Some(op ~ r) => MethodCall(l, Ident(op.chars, op.pos), List(r))
                                                                              case l ~ None => l }
  
  def multResult:Parser[Expr] = atom ~ opt(("+" | "-") ~ multResult) ^^ { case l ~ Some(op ~ r) => MethodCall(l, Ident(op.chars, op.pos), List(r))
                                                                          case l ~ None => l }
  
  def atom = parenExpr | int | char | call
  
  def parenExpr = "(" ~> expr <~ ")"
  
  def int = elem("integer literal", _.isInstanceOf[IntLiteral]) ^^ { tok =>
    val bi = BigInt(tok.chars)
    if(bi > Math.MAX_INT)
      throw ParserError("integer literal too big", tok.pos)
    else if(bi < Math.MIN_INT)
      throw ParserError("integer literal too small", tok.pos)
    else
      IntLit(bi.intValue, tok.pos)
  }
  
  def char = elem("character literal", _.isInstanceOf[CharLiteral]) ^^ { tok =>
    CharLit(tok.chars charAt 0, tok.pos)
  }
  
  def call = functionCall | methodCall | thisCall
  
  def functionCall = qident ~ opt(argsSpec) ^^ { case i ~ Some(as) => FunctionCall(i, as)
                                                 case i ~ None     => FunctionCall(i, Nil) }
  
  def methodCall = (expr <~ ".") ~ ident ~ opt(argsSpec) ^^ { case r ~ i ~ Some(as) => MethodCall(r, i, as)
                                                              case r ~ i ~ None     => MethodCall(r, i, Nil) }
  
  def thisCall = "this" ^^ { tok => FunctionCall(QIdent(List(Ident(tok.chars, tok.pos))), Nil) }
  
  def argsSpec = "(" ~> repsep(expr, ",") <~ ")"
  
  def ident = elem("identifier", _.isInstanceOf[Identifier]) ^^ { tok =>
    Ident(tok.chars, tok.pos)
  }
  
  def qident = rep1sep(ident, ".") ^^ { is => QIdent(is) }
  
  implicit def string2keyword(s:String):Parser[Elem] = acceptIf(_.chars == s)("`"+s+"' expected but " + _ + " found")
  
}

case class TypeSig(paramTypes:List[TypeExpr], returnType:TypeExpr)

case object CompilerPosition extends Position {
  def line = 0
  def column = 0
  def lineContents = ""
  override def longString = "[inside compiler]"
}
