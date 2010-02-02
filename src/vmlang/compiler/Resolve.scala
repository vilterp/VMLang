package vmlang.compiler

import ast._

/* TODO:
  - allow multiple functions & methods with different argument types
  - check for multiple method defs in each type spec
*/

object Resolve {
  
  type ModMap = Map[QIdent,ModuleDef]
  
  def apply(modules:List[ModuleDef]):Env =
    checkMultModDefs(modules) match {
      case Nil => (modules flatMap checkMultMemberDefs _) match {
        case Nil => (modules flatMap checkValidExports _) match {
          case Nil => {
            val modMap = Map() ++ (modules map { m => (m.name, m) })
            (modules flatMap { m => checkValidImports(m, modMap) }) match {
              case Nil => null //...
              case es => throw ResolutionErrors(es)
            }
          }
          case es  => throw ResolutionErrors(es)
        }
        case es  => throw ResolutionErrors(es)
      }
      case es => throw ResolutionErrors(es)
    }
  
  // def resolve(i:Ident, thisMod:ModuleDef, imps:List[Import], mods:ModMap):Either[ResolutionError,QIdent] =
  //       if(thisMod.defs exists { d => d.name == i })
  //         thisMod.name + i
  //       else
  //         
  //   
  def get(ident:QIdent, mods:ModMap):Either[InvalidImport,Def] =
      mods get ident match {
        case Some(mod) => Right(mod)
        case None      => ident dropRight 1 match {
          case QIdent(Nil)  => Left(NoSuchModule(ident))
          case init         => mods get init match {
            case Some(mod:ModuleDef) => mod.defs find { _.name == ident.last } match {
              case Some(d) => Right(d)
              case None    => Left(NoSuchMember(init, ident.last))
            }
            case None               => Left(NoSuchModule(init))
          }
        }
      }
  
  // def checkValidImports(mod:ModuleDef, mods:ModMap):List[InvalidImport] =
  def checkValidImports(mod:ModuleDef, mods:ModMap):List[InvalidImport] = Nil
  
  def checkValidExports(mod:ModuleDef):List[NonexistentExport] =
      mod.exports match {
        case e:ExportAll => Nil
        case ExportList(idents) => idents flatMap { i =>
          if(mod.defs contains i) Nil else List(NonexistentExport(mod.name, i)) }
      }
  
  def checkMultModDefs(mods:List[ModuleDef]):List[MultipleModuleDefs] =
      (getDups(mods) map { case (ident, defs) =>
              MultipleModuleDefs(ident.asInstanceOf[QIdent], defs map { d => d.pos }) }).toList
  
  def checkMultMemberDefs(mod:ModuleDef):List[MultipleMemberDefs] = {
      val functionDefs = mod.defs filter { d => d.isInstanceOf[FunctionDef] }
      val functionErrors = getDups(functionDefs) map { case (name, defs) =>
                        MultipleFunctionDefs(mod.name, name.asInstanceOf[Ident], defs map { d => d.pos }) }
      val typeDefs = mod.defs filter { d => d.isInstanceOf[TypeDef] }
      val typeErrors = getDups(typeDefs) map { case (name, defs) =>
                            MultipleTypeDefs(mod.name, name.asInstanceOf[Ident], defs map { d => d.pos }) }
      functionErrors.toList ::: typeErrors.toList
  }
  
  def getDups[A<:Def](items:List[A]):Map[Identifier,List[A]] =
      makeDefMap(Map(), items) filter { case (name, defs) => defs.length > 1 }
  
  def makeDefMap[A<:Def](already:Map[Identifier,List[A]], items:List[A]):Map[Identifier,List[A]] =
      items match {
        case Nil => already
        case x :: xs => already + (already get x.name match {
          case Some(defs) => (x.name, defs ::: List(x))
          case None       => (x.name, List(x))
        })
      }
  
}

/*
check that modules export things they actually define
check that modules import things that actually exist
report duplicate modules
report duplicate definitions
change all function calls to be fully-qualified names
report uses of nonexistent things
report uses of non-exported things
*/
