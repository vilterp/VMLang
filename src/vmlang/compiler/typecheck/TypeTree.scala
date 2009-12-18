package vmlang.compiler.typecheck

import vmlang.compiler.ast.TypeExpr

case class TypeTree(t:KnownType, subTypes:List[TypeTree]) {
  
  def add(superTypeName:String, newType:KnownType):TypeTree = findTree(superTypeName) match {
    case Some(tree) => tree add newType
    case None =>
      throw new NonexistentTypeError(superTypeName)
  }
  
  def add(newType:KnownType):TypeTree = TypeTree(t, TypeTree(newType,Nil) :: subTypes)
  
  def find(tn:String) = findTree(tn) match {
    case Some(tree) => tree.t
    case None =>
        throw new NonexistentTypeError(tn)
  }
  
  private def findTree(tn:String):Option[TypeTree] = {
    if (tn == t.name)
      Some(this)
    else if (isLeaf)
      None
    else
      subTypes.map(_ findTree tn).find(_.isInstanceOf[Some[TypeTree]]) match {
        case Some(tree) => tree
        case None => None
      }
  }
  
  def complies(expected:TypeExpr, given:TypeExpr):List[TypeCompliance] = complies(this,expected,given)
  
  private def complies(topLevel:TypeTree, expected:TypeExpr, given:TypeExpr):List[TypeCompliance] = 
    findTree(expected.name) match {
      case Some(expTree) =>
        expTree.findTree(given.name) match {
          case Some(givTree) => List(Complies)
          case None => topLevel findTree given.name match {
            case Some(_) => List(DoesntDescend(expected.name,given.name))
            case None => List(NonexistentType(given.name))
          }
        }
      case None => List(NonexistentType(expected.name))
    }
  
  def deepestCommonAncestor(a:Type, b:Type):Type = (a, b) match {
    case (a:KnownType, b:KnownType) => find(commonPath(a.name,b.name).last)
    case (_, _) => UnknownType
  }
  
  private def commonPath(a:String, b:String):List[String] =
      (path(a) zip path(b)) takeWhile { p => p._1 == p._2 } map { p => p._1 }
  
  private def path(tn:String):List[String] = path(List(),tn) match {
    case Some(l) => l
    case None => throw new NonexistentTypeError(tn)
  }
  
  private def path(path:List[String], tn:String):Option[List[String]] =
      if(tn == t.name)
        Some(path ::: List(tn))
      else if(isLeaf)
        None
      else
        subTypes.map(_.path(path,tn)).find(_.isInstanceOf[Some[List[String]]]) match {
          case Some(Some(p)) => Some(path ::: List(t.name) ::: p)
          case None => None
        }
  
  def prettyPrint:String = prettyPrint(0).mkString
  
  def prettyPrint(indent:Int):List[Char] =
          (" " * indent).toList ::: t.name.toList ::: List('\n') ::: (
                                subTypes flatMap { _ prettyPrint (indent + 2) })
  
  def isLeaf = subTypes.isEmpty
  
}
