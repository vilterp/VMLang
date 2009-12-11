package vmlang.compiler

import vmlang.compiler.ast._

object Test extends UnitTest {
  
  val tt = TypeTree(Trait("Value",0),List(
                    TypeTree(Trait("Ord",0),List(
                              TypeTree(Trait("Num",0),List(
                                       TypeTree(Class("Int",0),Nil),
                                       TypeTree(Class("Float",0),Nil))),
                              TypeTree(Class("Char",0),Nil))
                    ),
                    TypeTree(Class("List",1),Nil),
                    TypeTree(Class("Map",2),List(
                      TypeTree(Class("HashMap",2),Nil)))
                    ))
  
  implicit def string2typeExpr(s:String) = Parser.parseTypeExpr(s)
  
  def main(args:Array[String]) = {
    shouldBe(tt.complies("Value","Char"),List(Complies))
    shouldBe(tt.complies("Int","Int"),List(Complies))
    shouldBe(tt.complies("Int","Char"),List(DoesntDescend("Int","Char")))
    shouldBe(tt.complies("Int","Slartibartfast"),List(NonexistentType("Slartibartfast")))
    shouldBe(tt.complies("Map[Ord,Num]","HashMap[Char,Float]"),List(Complies))
    shouldBe(tt.complies("Map[Ord,Num]","HashMap[Ooj,Boo]"),
                            List(NonexistentType("Ooj"),NonexistentType("Boo")))
    shouldBe(tt.complies("List","List[Int]"),List(WrongNumTypeParams("List",1,0)))
  }
}

class UnitTest {
  
  def shouldBe(a:Any, b:Any) =
        if (a == b) println("pass") else println("fail. should be: " + b + "; found: " + a)
  
}