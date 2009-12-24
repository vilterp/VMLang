package vmlang.compiler

import vmlang.compiler.ast._

object Test extends UnitTest {
  
  def main(args:Array[String]) = {
    shouldBe(2,2)
  }
  
}

class UnitTest {
  
  def shouldBe(a:Any, b:Any) =
        if (a == b) println("pass") else println("fail. should be: " + b + "; found: " + a)
  
}