package vmlang.vm

import java.io.{File, FileInputStream, FileNotFoundException, IOException}
import vmlang.common.{OptParse, Args}
import collection.immutable.{Set, EmptySet}

object Main {
  
  // TODO: think up language name!
  val help =  "usage: *language name* <executable_file> <options>\n" +
              "-heap_size=<num bytes>      heap size in bytes\n" +
              "-stack_size=<num bytes>     stack size in bytes"
  
  def main(as:Array[String]) = {
    try {
      val args = OptParse(as)
      // check for extra arg pairs
      args.opts.keySet.toList -- List("heap_size","stack_size") match {
        case Nil =>
        case l:List[String] => {
          println("Unrecognized options: " + (l mkString ", "))
          exit
        }
      }
      // check for extra flags
      args.flags match {
        case s if s.isEmpty =>
        case s:Seq[String] => {
          println("Unrecognized flags: " + (s mkString ", "))
          exit
        }
      }
      // get file name
      val fileName = args.args match {
        case s if s.length == 1 => s.first
        case _ => {
          println("supply 1 file name")
          exit
        }
      }
      // get stack size
      val stackSize = args.opts get "stack_size" match {
        case Some(s) => try { s.toInt } catch {
          case e:NumberFormatException => "not a valid stack size number"
          exit
        }
        case None => 1024
      }
      // get heap size
      val heapSize = args.opts get "heap_size" match {
        case Some(s) => try { s.toInt } catch {
          case e:NumberFormatException => "not a valid heap size number"
          println(help)
          throw new FatalError
        }
        case None => 1024
      }
    
      // and, finally
      try {
        new VM(loadFileBytes(fileName),heapSize,stackSize).run()
      } catch {
        case e:StackOverflowError => { 
          println("VM Error: Stack Overflow")
          throw new FatalError
        }
        case e:IllegalArgumentException => {
          println("VM Error: " + e.getMessage)
          throw new FatalError
        }
      }
    } catch {
      case e:FatalError =>
    }
  }
  
  def loadFileBytes(fileName:String):Array[Byte] = {
    try {
      val in = new FileInputStream(new File(fileName))
      val result = new Array[Byte](in.available)
      in.read(result)
      result        
    } catch {
      case e:FileNotFoundException => {
        println("File not found: " + fileName)
        throw new FatalError
      }
      case e:IOException => {
        println("Error reading file " + fileName)
        throw new FatalError
      }
    }
  }
  
  def exit = {
    println(help)
    throw new FatalError
  }
  
}

class FatalError extends Exception
