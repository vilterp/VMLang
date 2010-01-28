package parse

import util.parsing.input.Position

abstract class ASTNode {
  def pos:Position
}
  case class ModuleDef(name:QIdent, exports:List[Ident], imports:List[Import], defs:List[Def]) extends ASTNode {
    def pos = name.pos
  }
  case class Import(ident:QIdent, all:Boolean) extends ASTNode {
    def pos = ident.pos
  }
  abstract class Def extends ASTNode
    abstract class FunctionDef extends Def
      case class FuncDef(name:Ident, paramSpecs:List[ParamSpec], returnType:TypeExpr, body:Expr) extends FunctionDef {
        def pos = name.pos
      }
      case class AbsMethodDef(name:Ident, paramTypeSpecs:List[TypeExpr], returnType:TypeExpr) extends FunctionDef {
        def pos = name.pos
      }
    abstract class TypeDef extends Def {
      val name:Ident
      val parent:QIdent
      val methodDefs:List[FunctionDef]
    }
      case class ClassDef(name:Ident, paramSpecs:List[ParamSpec], parent:QIdent, methodDefs:List[FuncDef]) extends TypeDef {
        def pos = name.pos
      }
      case class InterfaceDef(name:Ident, parent:QIdent, methodDefs:List[FunctionDef]) extends TypeDef {
        def pos = name.pos
      }
  abstract class Expr extends ASTNode
    case class Block(exprs:List[Expr], pos:Position) extends Expr
    case class LetExpr(bindings:List[(Ident,Expr)], body:Expr, pos:Position) extends Expr
    abstract class Literal extends Expr
      case class IntLit(i:Int, pos:Position) extends Literal
      case class FloatLit(f:Float, pos:Position) extends Literal
      case class CharLit(c:Char, pos:Position) extends Literal
    abstract class Call extends Expr
      case class MethodCall(receiver:Expr, name:Ident, args:List[Expr]) extends Call {
        def pos = name.pos
      }
      case class FunctionCall(name:QIdent, args:List[Expr]) extends Call {
        def pos = name.pos
      }
  case class TypeExpr(name:QIdent) extends ASTNode {
    def pos = name.pos
  }
  case class ParamSpec(name:Ident, t:TypeExpr) extends ASTNode {
    def pos = name.pos
  }
  case class QIdent(path:List[Ident]) extends ASTNode {
    override def toString = path mkString "."
    def pos = path.head.pos
  }
  case class Ident(name:String, pos:Position) extends ASTNode {
    override def toString = name
  }
