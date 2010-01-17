package vmlang.compiler

import java.io.{DataOutputStream, ByteArrayOutputStream}
import collection.mutable.HashMap


import vmlang.common.Opcodes
import vmlang.common.Opcodes._
import vmlang.compiler.ast._

// shameful imperative code...

object Linearize {
  
  def apply(e:Env):Array[Byte] = 
      null
  
}
// 
// class Linearizer(defs:List[Def], tt:TypeTree, ft:Map[String,TypeExpr]) {
//   
//   type Scope = List[(String, TypeExpr)]
//   
//   val baos = new ByteArrayOutputStream
//   val out = new DataOutputStream(baos)
//   val refs = new HashMap[Int,String]
//   val startingPoints = new HashMap[String,Int]
//   
//   val rtSizes = Map[String,Int]() ++ (defs map { d => (d.name, tt getSize d.returnType) })
//   val scopes = Map[String,Int]() ++
//                 (defs map { d => (d.name, d.params map { p => (p.name, tt getSize p.argType) }) })
//   
//   def run:Array[Byte] = {
//     wExpr(Call("main", Nil), Nil)
//     w(STOP)
//     for((n, d) <- defs) {
//       startingPoints += (n -> out.size)
//       // write def body expr
//       val scope = scopes(n)
//       wExpr(d.body, scope)
//       wDefPostlude(scope.foldLeft(0){ _ + _._2 }, rtSizes(n))
//     }
//     resolveGotos
//   }
//   
//   def resolveGotos:Array[Byte] = {
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
//   def wExpr(e:Expr, s:Scope):Unit = e match {
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
//       case Some(offset) => // is parameter
//           w(MOVE_FP_A)
//           w(I_CONST_B, offset)
//           w(I_SUB)
//           w(I_LOAD_A_A)
//           w(I_STORE_A_SP)
//           w(INC_SP_INT)
//       case None         => // is call
//           (args foreach { wExpr(_, s) })
//           wCallPrelude
//           wGoto(name)
//           wCallPostlude(scopes(name).foldLeft(0){ _ + _._2 }) // retrieve returned value
//     }
//     
//   }
//   
//   def argOffset(name:String, scope:Scope):Option[Int] =
//       (scope findIndexOf { _._1 == name }) match {
//         case -1    => None
//         case index => Some((scope drop index).foldLeft(0){ _ + _._2 })
//       }
//   
//   def wCallPrelude = {
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
//   def wGoto(ref:String) {
//     refs += (out.size -> ref)
//     w(GOTO, 0) // the 0 will be replaced in resolveGotos
//   }
//   
//   def wDefPostlude(paramSize:Int, rtSize:Int) = {
//     // decrement SP to point at return value
//     for(i <- (1 to (rtSize / 4)))
//       w(DEC_SP_INT)
//     for(i <- (1 to (rtSize % 4)))
//       w(DEC_SP)
//     // store answer on calling function's operand stack
//     w(MOVE_FP_A)
//     w(I_CONST_B, paramSize) // !!! what about functions w/ no params?!
//     w(I_SUB)
//     w(MOVE_A_B)
//     w(I_LOAD_A_SP)
//     w(I_STORE_A_B)
//     // set FP to saved FP (on stack)
//     w(DEC_SP_INT)
//     w(DEC_SP_INT)
//     w(I_LOAD_FP_SP) // load the value at addr SP into FP
//     // goto return address (on stack)
//     w(INC_SP_INT)
//     w(I_LOAD_A_SP)
//     w(GOTO_A)
//   }
//   
//   def getSize(e:Expr) =
//       tt.getSize(TypeCheck.inferType(e, tt, allFuncs))
//   
//   def wCallPostlude(paramSize:Int, rtSize:Int) = {
//     w(MOVE_SP_A)
//     w(I_CONST_B, 4 + (paramSize - rtSize))
//     w(I_SUB)
//     w(MOVE_A_SP)
//   }
//   
//   def cmpOp(a:Expr, b:Expr, s:Scope, cmp:()=>Unit) =
//       intOp(a, b, s, { w(I_SUB); cmp(); wPushByte })
//   
//   def intOp(a:Expr, b:Expr, s:Scope, ops:()=>Unit) = {
//     intOpNoPush(a, b, s, ops)
//     wPushInt
//   }
//   def intOpNoPush(a:Expr, b:Expr, s:Scope, ops:()=>Unit) = {
//     wExpr(a, s)
//     wExpr(b, s)
//     wPopIntB
//     wPopIntA
//     ops()
//   }
//   
//   def byteOp(a:Expr, b:Expr, s:Scope, ops:()=>Unit) = {
//     byteOpNoPush(a, b, s, ops)
//     wPushByte
//   }
//   def byteOpNoPush(a:Expr, b:Expr, s:Scope, ops:()=>Unit) = {
//     wExpr(a, s)
//     wExpr(b, s)
//     wPopByteB
//     wPopByteA
//     ops()
//   }
//   
//   def wPopIntA = { w(DEC_SP_INT); w(I_LOAD_A_SP) }
//   def wPopIntB = { w(DEC_SP_INT); w(I_LOAD_B_SP) }
//   def wPushInt = { w(I_STORE_A_SP); w(INC_SP_INT) }
//   
//   def wPopByteA = { w(DEC_SP); w(B_LOAD_A_SP) }
//   def wPopByteB = { w(DEC_SP); w(B_LOAD_B_SP) }
//   def wPushByte() = { w(DEC_SP); w(B_STORE_A_SP); }
//   
//   def w(oc:Opcodes):Unit = out.write(oc.toByte)
//   def w(oc:Opcodes, arg:Int):Unit = { w(oc); out.writeInt(arg) }
//   def w(oc:Opcodes, arg:Float):Unit = { w(oc); out.writeFloat(arg) }
//   def w(oc:Opcodes, arg:Byte):Unit = { w(oc); out.writeByte(arg) }
//   
// }
