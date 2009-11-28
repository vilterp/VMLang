package vmlang.compiler

import scala.util.parsing.combinator.syntactical._
import vmlang.compiler.ast._
import collection.immutable.HashMap

import java.util.Scanner

object Test extends Application {
  val s = new Scanner(System.in)
  print("> ")
  while(s.hasNext) {
    try {
      println(Parser(s.nextLine))
    } catch {
      case e:IllegalArgumentException => println(e.getMessage)
    }
    print("> ")
  }
}
