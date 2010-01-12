package vmlang.compiler.typecheck

import vmlang.compiler.ast.TypeExpr

// todo: make it work w/ type vars

case class TypeTree(t:Type, subTypes:List[TypeTree]) {
  
  def complies(exp:TypeExpr, giv:TypeExpr):List[TypeError] =
      (checkValidTypeExpr(exp) ::: checkValidTypeExpr(giv)) match {
        case Nil => descends(exp.name, giv.name) match {
          case true  => (exp.args zip giv.args) flatMap { p => complies(p._1, p._2) }
          case false => List(Mismatch(exp, giv))
        }
        case es => es
      }
  
  def descends(e:String, g:String):Boolean =
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
  
  def DCA(a:String, b:String):(String,Int) =
      ((path(a) zip path(b)) takeWhile { p => p._1 == p._2 }).last._1
  
  def path(tn:String):List[(String,Int)] =
      path(Nil,tn) match {
        case Some(l) => l
        case None => throw new NonexistentType(tn)
      }
  
  def path(path:List[(String,Int)], tn:String):Option[List[(String,Int)]] =
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
  
  def checkCompilableTypeExpr(te:TypeExpr, error:(TypeExpr)=>TypeError):List[TypeError] =
      findSubType(te.name) match {
        case Some(TypeTree(t, subTypes)) => t match {
          case at:AbsType  => if(allAreRefTypes(subTypes)) Nil else List(error(te))
          case pt:PrimType => Nil
        }
        case None => Nil // always called after checkValidTypeExpr
      }
  
  def allAreRefTypes(l:List[TypeTree]):Boolean =
      l forall { case TypeTree(t, subTypes) => t.isInstanceOf[RefType] && allAreRefTypes(subTypes) }
  
  def checkValidTypeExpr(te:TypeExpr):List[TypeError] =
      checkExistentAndNumArgs(te.name, te.args.length) :::
              (te.args flatMap { a => checkValidTypeExpr(a) })
  
  def getSize(te:TypeExpr):Int =
      find(te.name) match {
          // can assume that all subtypes of an AbsType will be reference types,
          // since typechecker checks all arguments and return type specs with checkCompilableTypeExpr
        case ty:AbsType => 4
        case ty:ConcreteType => ty.size 
      }
  
  def checkExistentAndNumArgs(tn:String, numArgs:Int):List[TypeError] =
      findSubType(tn) match {
        case None       => List(NonexistentType(tn))
        case Some(tree) => {
          val expNum = tree.t.numParams
          if(numArgs == expNum) Nil else List(WrongNumTypeArgs(expNum, numArgs))
        }
      }
  
  def findSubType(tn:String):Option[TypeTree] = {
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
  
  def find(tn:String):Type = {
    findSubType(tn) match {
      case Some(tt) => tt.t
      case None     => throw NonexistentType(tn)
    }
  }
  
  def isLeaf = subTypes.isEmpty
  
  def prettyPrint:String = prettyPrint(0).mkString
  
  def prettyPrint(indent:Int):List[Char] =
          (" " * indent).toList ::: t.name.toList ::: List('\n') ::: (
                                subTypes flatMap { _ prettyPrint (indent + 2) })
  
}
