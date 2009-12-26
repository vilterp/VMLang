package vmlang.compiler.typecheck

import vmlang.compiler.ast.{TypeExpr, NormalTypeExpr, FuncTypeExpr}

// could be more efficient: complies("Map[Num,Value]","TreeMap[Int,Float]") results in 44 calls to findSubType

case class TypeTree(t:Type, subTypes:List[TypeTree]) {
  
  def complies(exp:TypeExpr, giv:TypeExpr):List[TypeError] =
      (checkValidTypeExpr(exp) ::: checkValidTypeExpr(giv)) match {
        case Nil => (exp, giv) match {
          case (e:NormalTypeExpr, g:NormalTypeExpr) => compliesNormal(e, g)
          case (e:FuncTypeExpr  , g:FuncTypeExpr  ) => compliesFunc(e, g)
          case (e               , g               ) => List(Mismatch(e, g))
        }
        case es:List[TypeError] => es ::: ((exp, giv) match {
          case (e:NormalTypeExpr, g:NormalTypeExpr) => Nil
          case (e:FuncTypeExpr  , g:FuncTypeExpr  ) => Nil
          case (e               , g               ) => List(Mismatch(e, g))
        })
      }
  
  private def compliesNormal(exp:NormalTypeExpr, giv:NormalTypeExpr) =
      descends(exp.name, giv.name) match {
        case true  => (exp.args zip giv.args) flatMap { p => complies(p._1, p._2) }
        case false => List(Mismatch(exp, giv))
      }
  
  private def compliesFunc(exp:FuncTypeExpr, giv:FuncTypeExpr) = {
    val expPs = exp.paramTypes
    val givPs = giv.paramTypes
    complies(exp.returnType, giv.returnType) ::: (expPs.length == givPs.length match {
      case true  => (expPs zip givPs) flatMap { p => complies(p._1, p._2) }
      case false => List(WrongNumTypeArgs(expPs.length, givPs.length))
    })
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
  
  def deepestCommonAncestor(a:TypeExpr, b:TypeExpr):TypeExpr =
      (a, b) match {
        case (a:NormalTypeExpr, b:NormalTypeExpr) => normalDCA(a, b)
        case (a:FuncTypeExpr  , b:FuncTypeExpr  ) => funcDCA(a, b)
        case (a               , b               ) => throw TypeErrors(List(Mismatch(a, b)))
      }
  
  private def normalDCA(a:NormalTypeExpr, b:NormalTypeExpr):NormalTypeExpr = {
    // TODO: this isn't really right for situations with different numbers of type args
    val (name, numArgs) = DCA(a.name, b.name)
    NormalTypeExpr(name,
      ((a.args zip b.args) map { p => deepestCommonAncestor(p._1, p._2) }) take numArgs)
  }
  
  private def funcDCA(a:FuncTypeExpr, b:FuncTypeExpr):FuncTypeExpr = {
    val aps = a.paramTypes
    val bps = b.paramTypes
    (aps.length == bps.length) match {
      case true  => FuncTypeExpr((aps zip bps) map { p => deepestCommonAncestor(p._1, p._2) },
                                    deepestCommonAncestor(a.returnType, b.returnType))
      case false => throw TypeErrors(List(Mismatch(a, b)))
    }
  }
  
  private def DCA(a:String, b:String):(String,Int) =
      ((path(a) zip path(b)) takeWhile { p => p._1 == p._2 }).last._1
  
  private def path(tn:String):List[(String,Int)] =
      path(List(),tn) match {
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
      te match {
        case NormalTypeExpr(n, as) => checkExistentAndNumArgs(n, as.length) :::
                                            (as flatMap { a => checkValidTypeExpr(a) })
        case FuncTypeExpr(as, rt)  => (rt :: as) flatMap { a => checkValidTypeExpr(a) }
      }
  
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
