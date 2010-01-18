package vmlang.compiler

import vmlang.compiler.ast.{ Def, TypeExpr }
import vmlang.compiler.typecheck._

case class Env(defs:Map[String,Def], roots:Map[String,TypeExpr], tt:TypeTree) {
  
  lazy val ft = defs.foldLeft(roots){ (ft, d) => ft + (d._2.name -> d._2.typeExpr) }
  
  // for REPL only
  def addDef(d:Def) =
      if(roots contains d.name)
        throw RootDefError(d.name)
      else
        Env(defs + (d.name -> d), roots, tt)
  
}

object MakeEnv {
  
  def apply(defs:List[Def], roots:Map[String,TypeExpr], tt:TypeTree):Env =
      (checkForDuplicates(defs) ::: checkForRoots(defs, roots)) match {
        case Nil => Env(Map[String,Def]() ++ (defs map { d => (d.name, d) }), roots, tt)
        case es  => throw TypeErrors(es)
      }
  
  def checkForRoots(defs:List[Def], roots:Map[String,TypeExpr]):List[TypeError] =
      defs match {
        case Nil           => Nil
        case first :: rest =>
            if(roots contains first.name)
              RootDefError(first.name) :: checkForRoots(rest, roots)
            else
              checkForRoots(rest, roots)
      }
  
  def checkForDuplicates(defs:List[Def]):List[TypeError] =
      checkForDuplicates(Nil, defs) map { d => DuplicateDefError(d.name) }

  def checkForDuplicates(alreadyDefined:List[Def], defs:List[Def]):List[Def] =
      defs match {
        case Nil           => Nil
        case first :: rest => {
          if(alreadyDefined exists { _.name == first.name })
            first :: checkForDuplicates(alreadyDefined, rest)
          else
            checkForDuplicates(first :: alreadyDefined, rest)
        }
      }
  
}

