package vmlang.compiler

import vmlang.common.optparser._

import java.io.File

object Main extends OptParser {
  
  def numArgs(n:Int) = n == 1
  val argErrorMsg = "supply 1 file to compile"
  val knownFlags = List()
  val defaultOpts = Map[String,String]()
  val help = "usage: vmlc <source path>\n" +
             "  source path: \":\"-separated directory or file paths"
  
  // def run(args:List[String], flags:List[String], opts:Map[String,String]):Unit =
  //       try {
  //         writeFile(Compile(readFile(args.head)), (args.head split '.')(0) + ".vmlc")
  //       } catch {
  //         case e:CompilerError => throw FatalError(e.repr)
  //       }
  
  def run(args:List[String], flags:List[String], opts:Map[String,String]):Unit = {}
  
  def getAllFileContents(path:String, extension:String):List[String] =
      getAllFilePaths(path, extension) map { p => readFile(p) }
  
  def getAllFilePaths(path:String, extension:String):List[String] = {
    val f = new File(path)
    if(f.exists) {
      if(f.isDirectory)
        f.listFiles.toList flatMap { entry => getAllFilePaths(entry.getAbsolutePath, extension) }
      else if(f.getAbsolutePath endsWith extension)
        List(f.getAbsolutePath)
      else
        Nil
    } else
      throw FatalError("nonexistent file or directory: " + path)
  }
  
}
