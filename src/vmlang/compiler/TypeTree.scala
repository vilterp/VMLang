package vmlang.compiler

import vmlang.compiler.ast.TypeExpr

abstract case class Type(name:String, numParams:Int)
case class Trait(override val name:String, override val numParams:Int) extends Type(name,numParams)
case class Class(override val name:String, override val numParams:Int) extends Type(name,numParams)
            // expected "Class" to be reserved...

abstract class TypeCompliance
case object Complies extends TypeCompliance
abstract class DoesntComply extends TypeCompliance
case class NonexistentType(name:String) extends DoesntComply
case class DoesntDescend(expected:String,given:String) extends DoesntComply
case class WrongNumTypeParams(name:String,expected:Int,given:Int) extends DoesntComply

case class TypeTree(t:Type, subTypes:List[TypeTree]) {
  
  def add(superTypeName:String, newType:Type):TypeTree = find(superTypeName) match {
    case Some(tree) => tree add newType
    case None =>
      throw new IllegalArgumentException("Nonexistent type: " + superTypeName)
  }
  
  def add(newType:Type):TypeTree = TypeTree(t, TypeTree(newType,Nil) :: subTypes)
  
  def find(tn:String):Option[TypeTree] = {
    if (tn == t.name)
      Some(this)
    else if (isLeaf)
      None
    else
      subTypes.map(_ find tn).find(_.isInstanceOf[Some[TypeTree]]) match {
        case Some(tree) => tree
        case None => None
      }
  }
  
  def complies(expected:TypeExpr, given:TypeExpr):List[TypeCompliance] = complies(this,expected,given)
  
  private def complies(topLevel:TypeTree, expected:TypeExpr, given:TypeExpr):List[TypeCompliance] = 
    find(expected.name) match {
      case Some(expTree) =>
        if (expected.params.length != expTree.t.numParams)
          List(WrongNumTypeParams(expected.name,expTree.t.numParams,expected.params.length))
        else
          expTree.find(given.name) match {
            case Some(givTree) => 
                if(given.params.length == givTree.t.numParams) {
                  (expected.params zip given.params)
                           .flatMap{ p => complies(p._1, p._2) } removeDuplicates match {
                    case Nil => List(Complies)
                    case x :: xs => x :: xs
                  }
                } else
                  List(WrongNumTypeParams(given.name,givTree.t.numParams,given.params.length))
            case None => topLevel find given.name match {
              case Some(_) => List(DoesntDescend(expected.name,given.name))
              case None => List(NonexistentType(given.name))
            }
          }
      case None => List(NonexistentType(expected.name))
    }
  
  def prettyPrint:String = prettyPrint(0).mkString
  
  def prettyPrint(indent:Int):List[Char] =
          (" " * indent).toList ::: t.name.toList ::: List('\n') ::: (
                                subTypes flatMap { _ prettyPrint (indent + 2) })
  
  def isLeaf = subTypes.isEmpty
  
}
