package vmlang.common.optparser

import collection.immutable.HashMap

import java.io.{File, FileOutputStream, FileInputStream, FileNotFoundException, IOException}
import scala.io.Source

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
    (args, flags map { _ substring 1 }, opts)
  }
  
  def loadFileBytes(fileName:String):Array[Byte] = {
    try {
      val in = new FileInputStream(new File(fileName))
      val result = new Array[Byte](in.available)
      in.read(result)
      result        
    } catch {
      case e:FileNotFoundException =>
              throw FatalError("File not found: " + fileName)
      case e:IOException =>
              throw FatalError("Error reading file " + fileName)
    }
  }
  
  def writeFile(output:Array[Byte], fileName:String):Unit =
      try {
        new FileOutputStream(new File(fileName)).write(output)
      } catch {
        case e:FileNotFoundException => throw FatalError("Error creating file " + fileName)
        case e:IOException => throw FatalError("Error writing file " + fileName)
      }
  
  def writeFile(output:List[Byte], fileName:String):Unit =
      writeFile(output.toArray, fileName)
  
  def loadFile(fileName:String):String =
      try {
        Source.fromFile(fileName).getLines.mkString
      } catch {
        case e:FileNotFoundException => throw FatalError("File not found: " + fileName)
        case e:IOException => throw FatalError("Error reading file " + fileName)
      }
  
}

case class FatalError(msg:String) extends Exception
