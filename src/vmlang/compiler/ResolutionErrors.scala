package vmlang.compiler

import util.parsing.input.Position
import ast._

case class ResolutionErrors(errors:List[ResolutionError]) extends CompoundCompilerError(errors)

trait ResolutionError extends CompilerError
  case class NonexistentExport(module:QIdent, name:Ident) extends SinglePosCompilerError with ResolutionError {
    val message = "module " + module + " exports " + name + ", but doesn't define a member by that name"
    val pos = name.pos
  }
  abstract class InvalidImport extends SinglePosCompilerError with ResolutionError
    case class InvalidImportAll(i:ImportAll, member:Def) extends InvalidImport {
      val message = "cannot import all memebers of " + i + ", because it is " + member.toNounPhrase
      val pos = i.pos
    }
    case class NoSuchModule(i:QIdent) extends InvalidImport {
      val message = "imported identifier " + i + " does not exist or is not a module"
      val pos = i.pos
    }
    case class NoSuchMember(module:QIdent, i:Ident) extends InvalidImport {
      val message = "module " + module + " has no member " + i
      val pos = i.pos
    }
    case class UnimportableError(init:QIdent, d:Def, last:Ident) extends InvalidImport {
      val message = init + " has no member " + last + " (it is a " + d.toNounPhrase + ")"
      val pos = last.pos
    }
  abstract class MultipleDefsError extends MultPosCompilerError with ResolutionError
    case class MultipleModuleDefs(name:QIdent, positions:List[Position]) extends MultipleDefsError {
      val message = "multple definitions of module " + name
    }
    abstract class MultipleMemberDefs extends MultipleDefsError {
      val module:QIdent
      val name:Ident
    }
      case class MultipleFunctionDefs(module:QIdent, name:Ident, positions:List[Position]) extends MultipleMemberDefs {
        val message = "multiple definitions of function " + (module + name)
      }
      case class MultipleMethodDefs(module:QIdent, typeName:Ident, methodName:Ident, typeSig:List[TypeExpr], positions:List[Position]) extends MultipleMemberDefs {
        val message = "multiple definitions in type " + (module + typeName) + " of method " + name + typeSig.mkString("(",",",")")
        val name = methodName
      }
      case class MultipleTypeDefs(module:QIdent, name:Ident, positions:List[Position]) extends MultipleMemberDefs {
        val message = "multple definitions of type " + (module + name)
      }
