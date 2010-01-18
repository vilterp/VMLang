package vmlang.compiler

import collection.mutable.ArrayBuffer
import collection.mutable.HashMap

import vmlang.common.Opcodes
import vmlang.common.Opcodes._
import vmlang.compiler.ast._

// shameful imperative code...

object Linearize {
  
  def apply(e:Env):Array[Byte] = 
      new Linearizer(e).run
  
}

class Linearizer(e:Env) {
  
  type Scope = List[(String, Int)]
  
  val out = new ArrayBuffer[Byte]
  val refs = new HashMap[Int,String]
  val startingPoints = new HashMap[String,Int]
  val rtSizes = Map[String,Int]() ++ (e.defs map { case (n, d) => (n, e.tt getSize d.returnType) })
  val scopes = Map[String,Scope]() ++ (e.defs map { case (na, d) => (na, d.params map {
                                                      case ParamSpec(n, at) => (n, e.tt.getSize(at)) } ) })
  
  def run:Array[Byte] = {
    // initialize SP to stack start (given by command line argument)
    w(STACK_START)
    w(MOVE_A_SP)
    w(MOVE_A_FP)
    // write call to main
    wExpr(Call("main", Nil), Nil)
    w(STOP)
    // write definitions
    for((n, d) <- e.defs) {
      startingPoints += (n -> out.length)
      // write def body expr
      wExpr(d.body, scopes(n))
      wDefPostlude(rtSizes(n))
    }
    resolveGotos
  }
  
  def resolveGotos:Array[Byte] = {
    for((ind, ref) <- refs) {
      val refInd = startingPoints(ref)
      // write program index (integer) to result array
      overWriteInt(ind + 1, refInd)
    }
    out.toArray
  }
  
  def wExpr(e:Expr, s:Scope):Unit = { println("wExpr: " + e + " s: " + s); e match {
    
    case IntLit(i)   => w(I_CONST_A, i); wPushInt
    case CharLit(c)  => w(B_CONST_A, c.toByte); wPushByte
    case FloatLit(f) => w(I_CONST_A, java.lang.Float.floatToIntBits(f)); wPushInt
    
    case Call("+", List(a, b)) => intOp(a, b, s, ()=>{ w(I_ADD) })
    case Call("-", List(a, b)) => intOp(a, b, s, ()=>{ w(I_SUB) })
    case Call("*", List(a, b)) => intOp(a, b, s, ()=>{ w(I_MUL) })
    case Call("/", List(a, b)) => intOp(a, b, s, ()=>{ w(I_DIV) })
    case Call("%", List(a, b)) => intOp(a, b, s, ()=>{ w(I_MOD) })
    
    case Call("==", List(a, b))  => cmpOp(a, b, s, ()=>{ println("hello from =='s anon func"); w(EQ_A); })
    case Call(">" , List(a, b))  => cmpOp(a, b, s, ()=>{ w(GT_A) })
    case Call("<" , List(a, b))  => cmpOp(a, b, s, ()=>{ w(LT_A) })
    case Call(">=", List(a, b))  => cmpOp(a, b, s, ()=>{ w(LT_A); w(NEG_A) })
    case Call("<=", List(a, b))  => cmpOp(a, b, s, ()=>{ w(GT_A); w(NEG_A) })
    
    case Call("and", List(a, b)) => byteOp(a, b, s, ()=>{ w(AND) })
    case Call("or" , List(a, b)) => byteOp(a, b, s, ()=>{ w(OR) })
    case Call("not", List(a))    => wExpr(a, s); wPopByteA; w(NEG_A); wPushByte()
    
    case Call("true" , Nil) => w(B_CONST_A, 1:Byte); wPushByte
    case Call("false", Nil) => w(B_CONST_A, 0:Byte); wPushByte
    case Call("null" , Nil) => // do nothing
    
    case Call("printInt", List(a)) => wExpr(a, s); wPopIntA; w(PRINT_INT_A)
    
    case Call(name, args) => argOffset(name, s) match {
      case Some(offset) => // is parameter
          w(MOVE_FP_A)
          w(I_CONST_B, offset)
          w(I_SUB)
          w(I_LOAD_A_A)
          w(I_STORE_A_SP)
          w(INC_SP_INT)
      case None         => // is call
          (args foreach { wExpr(_, s) })
          wCallPrelude
          wGoto(name)
          wCallPostlude(scopes(name).foldLeft(0){ _ + _._2 }, rtSizes(name)) // retrieve returned value
    }
    
    case IfExpr(c, i, e) => {
        // write condition
        wExpr(c, s)
        wPopByteA
        // write conditional goto
        val condGotoInd = out.length
        w(GOTO_IF_NOT_A, 0)
        // write if expr
        wExpr(i, s)
        // write goto to after end of else expr
        val ifGotoInd = out.length
        w(GOTO, 0)
        // write else expr
        wExpr(e, s)
        val afterElseInd = out.length
        // overwrite conditional goto addr
        overWriteInt(condGotoInd + 1, ifGotoInd + 5)
        // overwrite after-if goto addr
        overWriteInt(ifGotoInd + 1, afterElseInd)
    }
    
  } }
  
  def argOffset(name:String, scope:Scope):Option[Int] =
      (scope findIndexOf { _._1 == name }) match {
        case -1    => None
        case index => Some((scope drop index).foldLeft(0){ _ + _._2 })
      }
  
  def wCallPrelude = {
    println("call prelude")
    // push base pointer
    w(MOVE_FP_A)
    w(I_STORE_A_SP)
    w(MOVE_SP_FP)
    w(INC_SP_INT)
    // push (program address+13)
      // so it will return to the instruction right after the GOTO
    w(I_CONST_A, out.length + 12)
    w(I_STORE_A_SP)
    w(INC_SP_INT)
  }
  
  def wGoto(ref:String) {
    refs += (out.length -> ref)
    w(GOTO, 0) // the 0 will be replaced in resolveGotos
  }
  
  def wDefPostlude(rtSize:Int) = {
    println("def postlude")
    // decrement SP to point at return value
    for(i <- (1 to (rtSize / 4)))
      w(DEC_SP_INT)
    for(i <- (1 to (rtSize % 4)))
      w(DEC_SP)
    // dec sp to return address
    w(DEC_SP_INT)
    // goto return address
    w(GOTO_SP)
  }
  
  def wCallPostlude(argSize:Int, rtSize:Int) = {
    println("call postlude")
    // dec sp to stored fp
    w(DEC_SP_INT)
    // load stored fp into fp
    w(I_LOAD_SP_FP)
    // dec sp past args
    w(DEC_SP_BY, argSize)
    // push answer (still in a)
    if(rtSize == 4)
      wPushInt
    else if(rtSize == 1)
      wPushByte
    else if(rtSize == 0)
      ()
    else
      throw new IllegalArgumentException("rtSize must be either 1 or 4")
        // this should never be thrown...
  }
  
  def cmpOp(a:Expr, b:Expr, s:Scope, cmp:()=>Unit) = {
      println("cmpOp: a: " + a + " b: " + b + " s: " + s)
      intOpNoPush(a, b, s, () => { println("hello from cmpOp's anon func"); w(I_SUB); cmp(); wPushByte() })
  }
  
  def intOp(a:Expr, b:Expr, s:Scope, ops:()=>Unit) = {
    intOpNoPush(a, b, s, ops)
    wPushInt
  }
  
  def intOpNoPush(a:Expr, b:Expr, s:Scope, ops:()=>Unit) = {
    println("intOpNoPush a: " + a + " b: " + b + " s: " + s)
    wExpr(a, s)
    wExpr(b, s)
    wPopIntB
    wPopIntA
    ops()
  }
  
  def byteOp(a:Expr, b:Expr, s:Scope, ops:()=>Unit) = {
    println("  byteOp: a: " + a + " b: " + b + " s: " + s)
    byteOpNoPush(a, b, s, ops)
    wPushByte
  }
  
  def byteOpNoPush(a:Expr, b:Expr, s:Scope, ops:()=>Unit) = {
    println("byteOpNoPush: a: " + a + " b: " + b + " s: " + s)
    wExpr(a, s)
    wExpr(b, s)
    wPopByteB
    wPopByteA
    ops()
  }
  
  def wPopIntA = { w(DEC_SP_INT); w(I_LOAD_SP_A) }
  def wPopIntB = { w(DEC_SP_INT); w(I_LOAD_SP_B) }
  def wPushInt = { w(I_STORE_A_SP); w(INC_SP_INT) }
  
  def wPopByteA = { w(DEC_SP); w(B_LOAD_SP_A) }
  def wPopByteB = { w(DEC_SP); w(B_LOAD_SP_B) }
  def wPushByte() = { w(B_STORE_A_SP); w(INC_SP) }
  
  def w(oc:Opcodes):Unit = { println("  " + oc); out += oc.toByte }
  def w(oc:Opcodes, arg:Int):Unit = { w(oc); wInt(arg) }
  def w(oc:Opcodes, arg:Float):Unit = { w(oc); wInt(java.lang.Float.floatToIntBits(arg)) }
  def w(oc:Opcodes, arg:Byte):Unit = { w(oc); out += arg }
  
  def wInt(i:Int) = {
    out += (0xff & (i >> 24)).asInstanceOf[Byte]
    out += (0xff & (i >> 16)).asInstanceOf[Byte]
    out += (0xff & (i >> 8)).asInstanceOf[Byte]
    out += (0xff & i).asInstanceOf[Byte]
  }
  
  def overWriteInt(addr:Int, i:Int) = {
    out(addr  ) = (0xff & (i >> 24)).asInstanceOf[Byte]
    out(addr+1) = (0xff & (i >> 16)).asInstanceOf[Byte]
    out(addr+2) = (0xff & (i >> 8)).asInstanceOf[Byte]
    out(addr+3) = (0xff & i).asInstanceOf[Byte]
  }
  
}
