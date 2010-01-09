// package vmlang.compiler
// 
// import java.io.{DataOutputStream, ByteArrayOutputStream}
// import collection.mutable.HashMap
// 
// 
// import vmlang.common.Opcodes
// import vmlang.common.Opcodes._
// import vmlang.compiler.ast._
// 
// // shameful imperative code...
// 
// object Linearize {
//   
//   def apply(prog:Prog):Array[Byte] = new Linearizer(prog).run
//   
// }
// 
// class Linearizer(prog:Prog) {
//   
//   private val baos = new ByteArrayOutputStream
//   private val out = new DataOutputStream(baos)
//   private val refs = new HashMap[Int,String]
//   private val startingPoints = new HashMap[String,Int]
//   
//   def run:Array[Byte] = {
//     wExpr(Call("main", Nil), Nil)
//     w(STOP)
//     for((n, d) <- prog.defs) {
//       startingPoints += (n -> out.size)
//       wExpr(d.body, d.params)
//       // goto return address (on stack)
//       w(MOVE_A_FP)
//       w(DEC_SP_INT)
//       w(I_LOAD_A_SP)
//       w(GOTO_A)
//     }
//     resolveGotos
//   }
//   
//   private def resolveGotos:Array[Byte] = {
//     val result = baos.toByteArray
//     for((ind, ref) <- refs) {
//       val refInd = startingPoints(ref)
//       // write program index (integer) to result array
//       result(ind+1) = (0xff & (refInd << 24)).asInstanceOf[Byte]
//       result(ind+2) = (0xff & (refInd << 16)).asInstanceOf[Byte]
//       result(ind+3) = (0xff & (refInd << 8)).asInstanceOf[Byte]
//       result(ind+4) = (0xff & refInd).asInstanceOf[Byte]
//     }
//     result
//   }
//   
//   private def w(oc:Opcodes):Unit = out.write(oc.toByte)
//   private def w(oc:Opcodes, arg:Int):Unit = { w(oc); out.writeInt(arg) }
//   private def w(oc:Opcodes, arg:Float):Unit = { w(oc); out.writeFloat(arg) }
//   private def w(oc:Opcodes, arg:Byte):Unit = { w(oc); out.writeByte(arg) }
//   
//   private def wExpr(e:Expr, s:Scope):Unit = e match {
//     
//     case IntLit(i)   => w(I_CONST_A, i); w(I_STORE_A_SP); w(INC_SP_INT)
//     case CharLit(c)  => w(B_CONST_A, c.toByte); w(B_STORE_A_SP); w(INC_SP)
//     case FloatLit(f) => w(I_CONST_A, java.lang.Float.floatToIntBits(f)); w(I_STORE_A_SP); w(INC_SP_INT)
//     
//     case Call("+", List(a, b)) => intOp(a, b, s, ()=>{ w(I_ADD) })
//     case Call("-", List(a, b)) => intOp(a, b, s, ()=>{ w(I_SUB) })
//     case Call("*", List(a, b)) => intOp(a, b, s, ()=>{ w(I_MUL) })
//     case Call("/", List(a, b)) => intOp(a, b, s, ()=>{ w(I_DIV) })
//     case Call("%", List(a, b)) => intOp(a, b, s, ()=>{ w(I_MOD) })
//     
//     case Call("==", List(a, b))  => cmpOp(a, b, s, ()=>{ w(EQ_A) })
//     case Call(">" , List(a, b))  => cmpOp(a, b, s, ()=>{ w(GT_A) })
//     case Call("<" , List(a, b))  => cmpOp(a, b, s, ()=>{ w(LT_A) })
//     case Call(">=", List(a, b))  => cmpOp(a, b, s, ()=>{ w(LT_A); w(NEG_A) })
//     case Call("<=", List(a, b))  => cmpOp(a, b, s, ()=>{ w(GT_A); w(NEG_A) })
//     
//     case Call("and", List(a, b)) => byteOp(a, b, s, ()=>{ w(AND) })
//     case Call("or" , List(a, b)) => byteOp(a, b, s, ()=>{ w(OR) })
//     case Call("not", List(a))    => wExpr(a, s); wPopByteA; w(NEG_A); wPushByte()
//     
//     case Call("true" , Nil) => w(B_CONST_A, 1:Byte); wPushByte
//     case Call("false", Nil) => w(B_CONST_A, 0:Byte); wPushByte
//     case Call("null" , Nil) => // do nothing
//     
//     case Call("printInt", List(a)) => wExpr(a, s); wPopIntA; w(PRINT_INT_A)
//     
//     case Call(name, args) => argOffset(name, s) match {
//       case Some(offset) => 
//           w(MOVE_FP_A)
//           w(I_CONST_B, offset)
//           w(I_SUB)
//           w(I_LOAD_A_A)
//           w(I_STORE_A_SP)
//           w(INC_SP_INT)
//       case None         => 
//           (args foreach { wExpr(_, s) })
//           wCallPrelude
//           wGoto(name)
//           wCallPostlude(prog(name).params.foldLeft(0){ _ + _._2}) // retrieve returned value
//     }
//     
//   }
//   
//   private def argOffset(name:String, scope:Scope):Option[Int] =
//       (scope findIndexOf { _._1 == name }) match {
//         case -1    => None
//         case index => Some((scope drop index).foldLeft(0){ _ + _._2 })
//       }
//   
//   private def wCallPrelude = {
//     // push base pointer
//     w(MOVE_FP_A)
//     w(I_STORE_A_SP)
//     w(MOVE_SP_FP)
//     w(INC_SP_INT)
//     // push (program address+13)
//       // so it will return to the instruction right after the GOTO
//     w(MOVE_COUNTER_A)
//     w(I_CONST_B, 13)
//     w(I_ADD)
//     w(I_STORE_A_SP)
//     w(INC_SP_INT)
//   }
//   
//   private def wGoto(ref:String) {
//     refs += (out.size -> ref)
//     w(GOTO, 0) // the 0 will be replaced in resolveGotos
//   }
//   
//   private def wCallPostlude(paramSize:Int) = {
//     // load answer into FP
//     w(INC_SP_INT)
//     w(I_LOAD_FP_SP)
//     // save answer from FP to stack
//     w(MOVE_SP_A)
//     w(I_CONST_B, paramSize + 8)
//     w(I_SUB)
//     w(MOVE_A_SP)
//     w(I_STORE_FP_SP)
//     // get FP from stack
//     w(MOVE_SP_A)
//     w(I_CONST_B, paramSize)
//     w(I_ADD)
//     w(MOVE_A_SP)
//     w(I_LOAD_FP_SP)
//     // move SP back to operand stack
//     w(MOVE_SP_A)
//     w(I_CONST_B, paramSize)
//     w(I_SUB)
//     w(MOVE_A_SP)
//   }
//   
//   private def cmpOp(a:Expr, b:Expr, s:Scope, cmp:()=>Unit) =
//       intOp(a, b, s, { w(I_SUB); cmp(); wPushByte })
//   
//   private def intOp(a:Expr, b:Expr, s:Scope, ops:()=>Unit) = {
//     intOpNoPush(a, b, s, ops)
//     wPushInt
//   }
//   private def intOpNoPush(a:Expr, b:Expr, s:Scope, ops:()=>Unit) = {
//     wExpr(a, s)
//     wExpr(b, s)
//     wPopIntB
//     wPopIntA
//     ops()
//   }
//   
//   private def byteOp(a:Expr, b:Expr, s:Scope, ops:()=>Unit) = {
//     byteOpNoPush(a, b, s, ops)
//     wPushByte
//   }
//   private def byteOpNoPush(a:Expr, b:Expr, s:Scope, ops:()=>Unit) = {
//     wExpr(a, s)
//     wExpr(b, s)
//     wPopByteB
//     wPopByteA
//     ops()
//   }
//   
//   private def wPopIntA = { w(DEC_SP_INT); w(I_LOAD_A_SP) }
//   private def wPopIntB = { w(DEC_SP_INT); w(I_LOAD_B_SP) }
//   private def wPushInt = { w(I_STORE_A_SP); w(INC_SP_INT) }
//   
//   private def wPopByteA = { w(DEC_SP); w(B_LOAD_A_SP) }
//   private def wPopByteB = { w(DEC_SP); w(B_LOAD_B_SP) }
//   private def wPushByte() = { w(DEC_SP); w(B_STORE_A_SP); }
//   
// }
