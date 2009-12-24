package vmlang.common

import collection.immutable.HashMap

abstract class OptParser {
  
  def numArgs(n:Int):Boolean
  val argErrorMsg:String
  val knownFlags:List[String]
  val defaultOpts:Map[String,String]
  val help:String
  
  def run(args:List[String], flags:List[String], opts:Map[String,String]):Unit
  
  def main(as:Array[String]) = {
    val (args, flags, opts) = parse(as)
    // check num args
    if(!numArgs(args.length)) {
      println(argErrorMsg)
      println(help)
    } else {
      // check for unknown flags
      flags -- knownFlags match {
        case Nil => {
          // check for unknown opts
          opts.keySet.toList -- defaultOpts.keySet.toList match {
            case Nil => {
              // get opts
              val allOpts = defaultOpts ++ opts
              // run
              try {
                run(args, flags, allOpts)
              } catch {
                case FatalError(msg) => println(msg)
              }
            }
            case l:List[String] => {
              println("Unknown options: " + (l mkString ", "))
              println(help)
            }
          }
        }
        case l:List[String] => {
          println("Unkown flags: " + (l mkString ", "))
          println(help)
        }
      }
    }
  }
  
  private def parse(as:Array[String]) = {
    val (dashStarts, args) = as.toList partition { _ startsWith "-" }
    val (optArgs, flags) = dashStarts partition { _ exists { _ == '=' } }
    val opts = new HashMap[String,String] ++
                            (optArgs map { a => {
                                val s = (a substring 1) split '=';
                                (s(0), s(1))
                            } } )
    (args, flags, opts)
  }
  
}

case class FatalError(msg:String) extends Exception
