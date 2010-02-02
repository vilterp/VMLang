package vmlang.compiler.ast

import util.parsing.input.Position
import vmlang.compiler.parse.CompilerPosition

abstract class ASTNode {
  val pos:Position
}
  abstract class Exports extends ASTNode
    case class ExportAll(pos:Position) extends Exports {
      override def toString = "ExportAll"
    }
    case class ExportList(exports:List[Ident]) extends Exports {
      val pos = exports match {
        case Nil => CompilerPosition
        case exs => exs.head.pos
      }
    }
  abstract class Import extends ASTNode {
    def ident:QIdent
    val pos = ident.pos
  }
    case class ImportMember(ident:QIdent) extends Import
    case class ImportAll(ident:QIdent) extends Import
  abstract class Def extends ASTNode {
    def name:Identifier
    val toNounPhrase:String
  }
    case class ModuleDef(name:QIdent, exports:Exports, imports:List[Import], defs:List[MemberDef]) extends Def {
      val pos = name.pos
      val toNounPhrase = "a module definition"
    }
    abstract class MemberDef extends Def {
      val name:Ident
    }
      abstract class FunctorDef extends MemberDef
        case class FunctionDef(name:Ident, paramSpecs:List[ParamSpec], returnType:TypeExpr, body:Expr) extends FunctorDef {
          val pos = name.pos
          val toNounPhrase = "a function definition"
        }
        case class AbsMethodDef(name:Ident, paramTypeSpecs:List[TypeExpr], returnType:TypeExpr) extends FunctorDef {
          val pos = name.pos
          val toNounPhrase = "an abstract method definition"
        }
      abstract class TypeDef extends MemberDef {
        val parent:QIdent
        val methodDefs:List[FunctorDef]
      }
        case class ClassDef(name:Ident, paramSpecs:List[ParamSpec], parent:QIdent, methodDefs:List[FunctionDef]) extends TypeDef {
          val pos = name.pos
          val toNounPhrase = "a class definition"
        }
        case class InterfaceDef(name:Ident, parent:QIdent, methodDefs:List[FunctorDef]) extends TypeDef {
          val pos = name.pos
          val toNounPhrase = "an interface definition"
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
        val pos = name.pos
      }
      case class FunctionCall(name:QIdent, args:List[Expr]) extends Call {
        val pos = name.pos
      }
  case class TypeExpr(name:QIdent) extends ASTNode {
    val pos = name.pos
    override def toString = name.toString
  }
  case class ParamSpec(name:Ident, t:TypeExpr) extends ASTNode {
    val pos = name.pos
  }
  abstract class Identifier
    case class QIdent(path:List[Ident]) extends Identifier {
      override def toString = path mkString "."
      val pos = path.head.pos
      def +(i:Ident) = QIdent(path ::: List(i))
      def dropRight(i:Int) = QIdent(path dropRight i)
      def last = path.last
    }
    case class Ident(name:String, pos:Position) extends Identifier {
      override def toString = name
    }
