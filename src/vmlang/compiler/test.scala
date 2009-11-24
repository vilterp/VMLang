package vmlang.compiler

import scala.util.parsing.combinator.syntactical._
import vmlang.compiler.ast._

object Something extends StandardTokenParsers {
	
	lexical.delimiters ++= List("(",")")
	
	def someRule = repsep(ident,",")
	
	def apply(in:String) = phrase(someRule)(new lexical.Scanner(in))
	
}

object Test extends Application {
	println(Parser("a = 2-2"))
}
