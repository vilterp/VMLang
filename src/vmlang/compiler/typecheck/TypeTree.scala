package vmlang.compiler.typecheck

import vmlang.compiler.ast.TypeExpr

// could be more efficient: complies("Map[Num,Value]","TreeMap[Int,Float]") results in 44 calls to findTree

case class TypeTree(t:ExprType, subTypes:List[TypeTree]) {
  
  def find(tn:String) =
    findTree(tn) match {
      case Some(tree) => tree.t
      case None =>
          throw new NonexistentType(tn)
    }
  
  private def findTree(tn:String):Option[TypeTree] = {
    println(t.name + " findTree " + tn)
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
  
  def complies(expected:TypeExpr, given:TypeExpr):List[TypeError] =
      complies(this,expected,given)
  
  private def complies(topLevel:TypeTree, expected:TypeExpr, given:TypeExpr):List[TypeError] = 
    findTree(expected.name) match {
      // expected type exists
      case Some(expTree) =>
        expTree findTree given.name match {
          // given type descends from expected type
          case Some(givTree) => cmpTypeArgs(expected.args,expTree.t,given.args,givTree.t,topLevel)
          // given type doesn't descend from expected type
          case None => topLevel findTree given.name match {
            // given type doesn't descend, but exists
            case Some(givTree) => List(Mismatch(expected,given)) :::
                  cmpTypeArgs(expected.args,expTree.t,given.args,givTree.t,topLevel)
            // given type doesn't exist
            case None => List(NonexistentType(given.name))
          }
        }
      // expected type doesn't exist
      case None => List(NonexistentType(expected.name)) ::: (topLevel findTree given.name match {
        // given type exists
        case Some(givTree) => cmpNumTypeArgs(givTree.t, given.args)
        // neither type exists
        case None => List(NonexistentType(given.name))
      })
    }
  
  private def cmpTypeArgs(exp:List[TypeExpr], expType:ExprType,
                          giv:List[TypeExpr], givType:ExprType,
                                              topLevel:TypeTree):List[TypeError] =
      ((exp zip giv) flatMap { p => topLevel.complies(p._1, p._2) }) :::
          cmpNumTypeArgs(expType, exp) ::: cmpNumTypeArgs(givType, giv) :::
          checkExistent(exp, topLevel) ::: checkExistent(giv, topLevel)
  
  private def checkExistent(ts:List[TypeExpr], topLevel:TypeTree):List[TypeError] =
      ts flatMap { t => topLevel findTree t.name match {
        case Some(_) => Nil
        case None => List(NonexistentType(t.name))
      } }
  
  private def cmpNumTypeArgs(t:ExprType, tas:List[TypeExpr]):List[TypeError] =
      if(t.numParams == tas.length)
        Nil
      else
        List(WrongNumTypeArgs(t.numParams, tas.length))
  
  // def deepestCommonAncestor(a:TypeExpr, b:TypeExpr):TypeExpr =
  //     find(commonPath(a.name,b.name).last)
  // 
  // private def commonPath(a:String, b:String):List[String] =
  //     (path(a) zip path(b)) takeWhile { p => p._1 == p._2 } map { p => p._1 }
  // 
  // private def path(tn:String):List[String] =
  //     path(List(),tn) match {
  //       case Some(l) => l
  //       case None => throw new NonexistentType(tn)
  //     }
  // 
  // private def path(path:List[String], tn:String):Option[List[String]] =
  //     if(tn == t.name)
  //       Some(path ::: List(tn))
  //     else if(isLeaf)
  //       None
  //     else
  //       subTypes.map(_.path(path,tn)).find(_.isInstanceOf[Some[List[String]]]) match {
  //         case Some(Some(p)) => Some(path ::: List(t.name) ::: p)
  //         case None => None
  //       }
  
  def prettyPrint:String = prettyPrint(0).mkString
  
  def prettyPrint(indent:Int):List[Char] =
          (" " * indent).toList ::: t.name.toList ::: List('\n') ::: (
                                subTypes flatMap { _ prettyPrint (indent + 2) })
  
  def isLeaf = subTypes.isEmpty
  
}
