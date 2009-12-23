// package vmlang.compiler.typecheck
// 
// import vmlang.compiler.ast._
// import collection.immutable.HashMap
// 
// object TypeCheck {
//   
//   val typeTree =  TypeTree(AbsType("Value"),List(
//                       TypeTree(AbsType("Eq"),List(
//                         TypeTree(PrimType("Bool"),Nil),
//                         TypeTree(PrimType("Null"),Nil),
//                         TypeTree(AbsType("Ord"),List(
//                             TypeTree(AbsType("Num"),List(
//                                 TypeTree(PrimType("Int"),Nil),
//                                 TypeTree(PrimType("Float"),Nil))),
//                             TypeTree(PrimType("Char"),Nil)))))))
//   
//   type FuncTable = HashMap[String,FuncType]
//   
//   implicit def string2typeExpr(s:String) = Parser.parseTypeExpr(s).name
//   
//   val funcTable = HashMap(
//     "+" -> FuncType(List("Num","Num"),"Num"),
//     "-" -> FuncType(List("Num","Num"),"Num"),
//     "*" -> FuncType(List("Num","Num"),"Num"),
//     "/" -> FuncType(List("Num","Num"),"Num"),
//     "==" -> FuncType(List("Eq","Eq"),"Bool"),
//     "!=" -> FuncType(List("Eq","Eq"),"Bool"),
//     ">" -> FuncType(List("Ord","Ord"),"Bool"),
//     ">=" -> FuncType(List("Ord","Ord"),"Bool"),
//     "<" -> FuncType(List("Ord","Ord"),"Bool"),
//     "<=" -> FuncType(List("Ord","Ord"),"Bool"),
//     "true" -> FuncType(Nil,"Bool"),
//     "false" -> FuncType(Nil,"Bool"),
//     "null" -> FuncType(Nil,"Null"),
//     "printChar" -> FuncType(List("Char"),"Null"),
//     "getChar" -> FuncType(Nil,"Char")
//   )
//   
//   def apply(prog:Prog):Prog =
//       apply(prog, funcTable, typeTree)
//   
//   def apply(prog:Prog, funcTypes:FuncTable, typeTree:TypeTree):Prog = 
//       checkCompliance(prog,addTypeSigs(prog,funcTypes),typeTree)
//   
//   private def addTypeSigs(p:Prog, ft:FuncTable):FuncTable =
//       p.defs.foldLeft(ft){ (ft,d) => ft + (d.name -> FuncType(d.args,d.returnType)) }
//   
//   private def checkCompliance(p:Prog, ft:FuncTable, tt:TypeTree):Prog =
//       (p.defs flatMap { checkDef(_,ft,tt) }) match {
//         case Complies :: Nil => p
//         case errors:List[DoesntComply] => throw new TypeErrors // ...
//       }
//   
//   private def checkDef(d:Def, ft:FuncTable, tt:TypeTree):List[TypeCompliance] =
//       tt.complies(d.returnType,inferType(d.body)) // Type vs. TypeExpr ...
//   
//   private def inferType(expr:Expr, funcTypes:FuncTable, typeTree:TypeTree):Type =
//     expr match {
//       case IntLit(_) => typeTree find "Int"
//       case CharLit(_) => typeTree find "Char"
//       case IfExpr(_,i,t) => typeTree.deepestCommonAncestor(inferType(i,funcTypes,typeTree),
//                                                            inferType(t,funcTypes,typeTree))
//       case Call(name,args) => funcTypes get name match {
//         case Some(ft) => ft.returnType // TODO: check compliance of arguments - how to report all at once?
//         case None => UnknownType
//       }
//     }
//   
// }
