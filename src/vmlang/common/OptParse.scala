package vmlang.common

import collection.immutable.{Map, HashMap}

case class Args(args:Seq[String], flags:Seq[String], opts:Map[String,String])

object OptParse {
  
  def apply(input:Array[String]):Args = {
    val (dashStarts, args) = input partition { _ startsWith "-" }
    val (optArgs, flags) = dashStarts partition { _ exists { _ == '=' } }
    val opts = new HashMap[String,String] ++
          ((optArgs map { s => ((s substring 1) split '=') }) map { a => (a(0), a(1)) })
    Args(args,flags,opts)
  }
  
}