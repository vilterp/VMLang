package vmlang.compiler

import ast._

case class Env(funcDefs:Map[QIdent,Function], typeTree:TypeTree)
case class TypeTree(t:Type, subTypes:List[TypeTree])

abstract class Type {
  val name:Ident
  val methods:Map[Ident,Method]
}
  case class Interface(name:Ident, methods:Map[Ident,Method]) extends Type
  abstract class Class extends Type {
    val name:Ident
    val methods:Map[Ident,Method]
  }
    case class NativeClass(name:Ident, methods:Map[Ident,NativeMethod], size:Int) extends Class
    case class UserDefinedClass(name:Ident, methods:Map[Ident,ConcreteMethod], data:List[(String,TypeExpr)]) extends Class

abstract class Functor {
  def paramTypes:List[TypeExpr]
  def returnType:TypeExpr
}
  abstract class Function extends Functor
    case class UserDefinedFunction(paramSpecs:List[(String,TypeExpr)], returnType:TypeExpr, body:Expr) extends Function {
      def paramTypes = paramSpecs map { case (n, t) => t }
    }
    case class NativeFunction(paramTypes:List[TypeExpr], returnType:TypeExpr) extends Function
  abstract class Method extends Functor
    case class AbsMethod(paramTypes:List[TypeExpr], returnType:TypeExpr) extends Method
    abstract class ConcreteMethod extends Method
      case class UserDefinedMethod(paramSpecs:List[(String,TypeExpr)], returnType:TypeExpr, body:Expr) extends ConcreteMethod {
        def paramTypes = paramSpecs map { case (n, t) => t }
      }
      case class NativeMethod(paramTypes:List[TypeExpr], returnType:TypeExpr) extends ConcreteMethod
