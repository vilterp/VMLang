package vmlang.compiler.typecheck

abstract class Type {
  val name:String
  val numParams:Int
  override def toString = name
}
case class AbsType(name:String, numParams:Int) extends Type
abstract class ConcreteType extends Type {
  val size:Int
}
case class RefType(name:String, numParams:Int) extends ConcreteType {
  val size = 4
}
case class PrimType(name:String, size:Int) extends ConcreteType {
  val numParams = 0
}

/*

Type
  [AbsType]
  ConcreteType
    [RefType]
    [PrimType]

*/
