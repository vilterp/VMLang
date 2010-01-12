package vmlang.compiler

import vmlang.compiler.ast.{ Def, TypeExpr }
import vmlang.compiler.typecheck.TypeTree

case class Env(defs:Map[String,Def], ft:Map[String,TypeExpr], tt:TypeTree)
