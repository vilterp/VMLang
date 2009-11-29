package vmlang.compiler

object Test extends Application {
  val t = TypeTree("Value",List(TypeTree("Num",List(TypeTree("Int",Nil),TypeTree("Float",Nil)))))
  println(t prettyPrint)
  println(t.complies("Value","Int"))
  println(t.complies("Value","Floob"))
}
