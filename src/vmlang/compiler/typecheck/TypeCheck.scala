// package vmlang.compiler.typecheck
// 
// import vmlang.compiler.ast._
// import collection.immutable.HashMap
// 
// // TODO: check expr tree for calls to nonexistent functions first
//     // otherwise, maps and stuff are hard
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
//   val rootFuncTypes = HashMap(
//     "+"            ->      FuncType(List("Num","Num"),"Num"),
//     "-"            ->      FuncType(List("Num","Num"),"Num"),
//     "*"            ->      FuncType(List("Num","Num"),"Num"),
//     "/"            ->      FuncType(List("Num","Num"),"Num"),
//     "=="           ->      FuncType(List("Eq","Eq"),"Bool"),
//     "!="           ->      FuncType(List("Eq","Eq"),"Bool"),
//     ">"            ->      FuncType(List("Ord","Ord"),"Bool"),
//     ">="           ->      FuncType(List("Ord","Ord"),"Bool"),
//     "<"            ->      FuncType(List("Ord","Ord"),"Bool"),
//     "<="           ->      FuncType(List("Ord","Ord"),"Bool"),
//     "true"         ->      FuncType(Nil,"Bool"),
//     "false"        ->      FuncType(Nil,"Bool"),
//     "null"         ->      FuncType(Nil,"Null"),
//     "and"          ->      FuncType(List("Bool","Bool"),"Bool"),
//     "or"           ->      FuncType(List("Bool","Bool"),"Bool"),
//     "printChar"    ->      FuncType(List("Char"),"Null"),
//     "getChar"      ->      FuncType(Nil,"Char"),
//     "stackStart"   ->      FuncType(Nil,"Int")
//   )
//   
//   def apply(prog:Prog):Prog =
//       apply(prog, rootFuncTypes, typeTree)
//   
//   def apply(prog:Prog, ft:FuncTable, tt:TypeTree):Prog = 
//       checkCompliance(prog, addTypeSigs(prog, ft), tt)
//   
//   private def addTypeSigs(p:Prog, ft:FuncTable):FuncTable =
//       p.defs.foldLeft(ft){ (ft,d) => ft + (d.name -> FuncType(d.args, d.returnType)) }
//   
//   private def checkCompliance(p:Prog, ft:FuncTable, tt:TypeTree):Prog =
//       (p.defs flatMap { checkDef(_, ft, tt) }) match {
//         case Nil => p
//         case l:List[CompilerError] => throw new TypeErrors(l)
//       }
//   
//   private def checkDef(d:Def, ft:FuncTable, tt:TypeTree):List[CompilerError] =
//       tt.complies(d.returnType, inferType(d.body)) // Type vs. TypeExpr ...
//   
//   def checkCall(call:Call, ft:FuncTable, tt:TypeTree):List[CompilerError] = {
//     val giv = call.args
//     ft.get(call.name) match {
//       case Some(ft) => {
//         val exp = ft.paramTypes
//         if(exp.length == giv.length) Nil else List(WrongNumCallArgs(exp.length, giv.length)) :::
//             ((exp zip (giv map { inferType(_) })) map { p => tt.complies(p._1,p._2) })
//       }
//       case None => List(NonexistentFuncError(call.name))
//     }
//   }
//   
//   def inferType(expr:Expr, funcTypes:FuncTable, typeTree:TypeTree):TypeExpr =
//       inferType(expr, Map(), funcTypes, typeTree)
//   
//   // TODO: check function scope when inferring call
//   def inferType(expr:Expr, scope:Map[String,Type], ft:FuncTable, tt:TypeTree):TypeExpr =
//     expr match {
//       case IntLit(_) => tt find "Int"
//       case CharLit(_) => tt find "Char"
//       case IfExpr(_, i, t) => typeTree.deepestCommonAncestor(inferType(i, funcTypes, typeTree),
//                                                              inferType(t, funcTypes, typeTree))
//       case c:Call => if(isParam(c, scope)) scope(c.name) else ft(c.name).returnType
//     }
//   
//   private def isParam(call:Call, scope:Map[String,Type]):Boolean =
//       call.params.isEmpty && scope isDefinedAt call.name
//   
// }
