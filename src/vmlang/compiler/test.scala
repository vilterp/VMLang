package vmlang.compiler

import vmlang.compiler.ast._

object Test extends UnitTest {
  
  val tt = TypeTree(AbsType("Value"),List(
                    TypeTree(AbsType("Ord"),List(
                              TypeTree(AbsType("Num"),List(
                                       TypeTree(PrimType("Int"),Nil),
                                       TypeTree(PrimType("Float"),Nil))),
                              TypeTree(PrimType("Char"),Nil))
                    ),
                    TypeTree(RefType("List"),Nil),
                    TypeTree(AbsType("Map"),List(
                      TypeTree(RefType("HashMap"),Nil)))
                    ))
  
  implicit def string2typeExpr(s:String) = Parser.parseTypeExpr(s)
  
  implicit def string2type(s:String) = tt find s
  
  def main(args:Array[String]) = {
    shouldBe(tt.complies("Value","Char"),List(Complies))
    shouldBe(tt.complies("Int","Int"),List(Complies))
    shouldBe(tt.complies("Int","Char"),List(DoesntDescend("Int","Char")))
    shouldBe(tt.complies("Int","Slartibartfast"),List(NonexistentType("Slartibartfast")))
    
    shouldBe(tt.deepestCommonAncestor("Int","Int"),tt find "Int")
    shouldBe(tt.deepestCommonAncestor("Int","Float"),tt find "Num")
    shouldBe(tt.deepestCommonAncestor("Char","Int"),tt find "Ord")
    shouldBe(tt.deepestCommonAncestor("Num","List"),tt find "Value")
  }
}

class UnitTest {
  
  def shouldBe(a:Any, b:Any) =
        if (a == b) println("pass") else println("fail. should be: " + b + "; found: " + a)
  
}