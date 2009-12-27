package vmlang.compiler.typecheck

import vmlang.compiler.ast.TypeExpr

// could be more efficient: complies("Map[Num,Value]","TreeMap[Int,Float]") results in 44 calls to findSubType

case class TypeTree(t:Type, subTypes:List[TypeTree]) {
  
  def complies(exp:TypeExpr, giv:TypeExpr):List[TypeError] =
      (checkValidTypeExpr(exp) ::: checkValidTypeExpr(giv)) match {
        case Nil => descends(exp.name, giv.name) match {
          case true  => (exp.args zip giv.args) flatMap { p => complies(p._1, p._2) }
          case false => List(Mismatch(exp, giv))
        }
        case es:List[TypeError] => es
      }
  
  private def descends(e:String, g:String):Boolean =
      this findSubType e match {
        case Some(eTree) => eTree findSubType g match {
          case Some(gTree) => true
          case None        => false
        }
        case None => throw new IllegalArgumentException("nonexistent type: " + e)
      }
  
  def deepestCommonAncestor(l:List[TypeExpr]):TypeExpr =
      l match {
        case Nil      => throw new IllegalArgumentException("can't give DCA of empty list")
        case t :: Nil => t
        case t :: ts  => deepestCommonAncestor(t,deepestCommonAncestor(ts))
      }
  
  def deepestCommonAncestor(a:TypeExpr, b:TypeExpr):TypeExpr = {
    // TODO: this isn't really right for situations with different numbers of type args
    val (name, numArgs) = DCA(a.name, b.name)
    TypeExpr(name,
      ((a.args zip b.args) map { p => deepestCommonAncestor(p._1, p._2) }) take numArgs)
  }
  
  private def DCA(a:String, b:String):(String,Int) =
      ((path(a) zip path(b)) takeWhile { p => p._1 == p._2 }).last._1
  
  private def path(tn:String):List[(String,Int)] =
      path(Nil,tn) match {
        case Some(l) => l
        case None => throw new NonexistentType(tn)
      }
  
  private def path(path:List[(String,Int)], tn:String):Option[List[(String,Int)]] =
      if(tn == t.name)
        Some(path ::: List((t.name,t.numParams)))
      else if(isLeaf)
        None
      else
        subTypes.map(_.path(path,tn)).find(_.isInstanceOf[Some[List[String]]]) match {
          case Some(Some(p)) => Some(path ::: List((t.name,t.numParams)) ::: p)
          case Some(None) => None // would never happen...
          case None => None
        }
  
  private def checkValidTypeExpr(te:TypeExpr):List[TypeError] =
        checkExistentAndNumArgs(te.name, te.args.length) ::: (te.args flatMap { a => checkValidTypeExpr(a) })
  
  private def checkExistentAndNumArgs(tn:String, numArgs:Int):List[TypeError] =
      findSubType(tn) match {
        case None       => List(NonexistentType(tn))
        case Some(tree) => {
          val expNum = tree.t.numParams
          if(numArgs == expNum) Nil else List(WrongNumTypeArgs(expNum, numArgs))
        }
      }
  
  private def findSubType(tn:String):Option[TypeTree] = {
    if(tn == t.name)
      Some(this)
    else if(isLeaf)
      None
    else
      subTypes.map(_ findSubType tn).find(_.isInstanceOf[Some[TypeTree]]) match {
        case Some(tree) => tree
        case None => None
      }
  }
  
  def isLeaf = subTypes.isEmpty
  
  def prettyPrint:String = prettyPrint(0).mkString
  
  def prettyPrint(indent:Int):List[Char] =
          (" " * indent).toList ::: t.name.toList ::: List('\n') ::: (
                                subTypes flatMap { _ prettyPrint (indent + 2) })
  
}
