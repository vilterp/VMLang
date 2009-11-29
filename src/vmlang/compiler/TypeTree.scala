package vmlang.compiler

case class TypeTree(typeName:String, subTypes:List[TypeTree]) {
  
  def contains(t:String) = find(t) match {
    case Some(tree) => true
    case None => false
  }
  
  def find(t:String):Option[TypeTree] = {
    if (t == typeName)
      Some(this)
    else if (isLeaf)
      None
    else
      subTypes.map(_ find t).find(_.isInstanceOf[Some[TypeTree]]) match {
        case Some(tree) => tree
        case None => None
      }
  }
  
  def complies(expected:String, given:String) = find(expected) match {
    case Some(exp) => exp contains given
    case None => false
  }
  
  def prettyPrint:String = prettyPrint(0).mkString
  
  def prettyPrint(indent:Int):List[Char] =
          (" " * indent).toList ::: typeName.toList ::: List('\n') ::: (
                                subTypes flatMap { _ prettyPrint (indent + 2) })
  
  def isLeaf = subTypes.isEmpty
  
}
