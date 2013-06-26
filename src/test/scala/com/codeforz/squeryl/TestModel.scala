package com.codeforz.squeryl

import org.squeryl.{KeyedEntity, Schema}

case class Thing(id:Long, name:String) extends KeyedEntity[Long]
/**
 *
 */
object TestModel extends Schema with SchemaVersioning{
  def version = 1
  val things = table[Thing]
}
