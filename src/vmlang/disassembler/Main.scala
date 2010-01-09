package vmlang.disassembler

import vmlang.common.{OptParser, FatalError, Opcodes}
import vmlang.common.Opcodes._

import java.io.{File, FileInputStream, DataInputStream, FileNotFoundException, IOException, EOFException}

object Main extends OptParser {
  
  def numArgs(n:Int) = n == 1
  val argErrorMsg = "supply 1 bytecode file to disassemble"
  val knownFlags = List()
  val defaultOpts = Map[String,String]()
  val help = "usage: vmld <bytecode file>\n"
  
  def run(args:List[String], flags:List[String], opts:Map[String,String]):Unit =
      disassemble(args.head)
  
  def disassemble(fileName:String) =
    try {
      var addr = 0
      val in = new DataInputStream(new FileInputStream(new File(fileName)))
      val ocs = Opcodes.values
      while(in.available > 0) {
        print(addr + "\t")
        val oc = ocs(in.readByte)
        addr += 1
        print(oc + "\t")
        oc match {
          case I_CONST_A => print(in.readInt); addr += 4
          case I_CONST_B => print(in.readInt); addr += 4
          case B_CONST_A => print(in.readByte); addr += 1
          case B_CONST_B => print(in.readByte); addr += 1
          case GOTO      => print(in.readInt); addr += 4
          case _         =>
        }
        println
      }
    } catch {
      case e:FileNotFoundException => throw FatalError("File not found: " + fileName)
      case e:EOFException => throw FatalError("malformed program")
      case e:ArrayIndexOutOfBoundsException => throw FatalError("malformed program")
      case e:IOException => throw FatalError("Error reading file " + fileName)
    }
  
}
