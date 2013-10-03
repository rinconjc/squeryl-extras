package com.codeforz.squeryl

import org.squeryl.{Session, Table, KeyedEntity, Schema}
import org.squeryl.PrimitiveTypeMode._
import java.sql.Connection
import io.Source
import java.util.UUID
import grizzled.slf4j.Logging
import java.io.PrintWriter

private[squeryl] case class SchemaInfo(id:String, version:Int) extends KeyedEntity[String]
/**
 * 
 */
trait SchemaVersioning extends Schema with Logging{

  protected val schemaInfo = table[SchemaInfo]

  private val schemaDir = "/schema/"

  def version:Int

  def printSchema(pw:PrintWriter){
    inTransaction{
      printDdl(pw)
    }
  }

  def withConnection[T](f:(Connection)=>T):T = inTransaction{
    val con = Session.currentSession.connection
    try{
      f(con)
    }catch{
      case e: Exception =>
        error("Failed JDBC operation", e)
        throw e
    }
  }

  protected def exec(file:String){
    Option(getClass.getResourceAsStream(schemaDir + file)) foreach{is=>
      withConnection{con=>
        Source.fromInputStream(is).mkString.split(";") foreach{stmt =>
          if(!stmt.trim.isEmpty) con.createStatement().execute(stmt)
        }
      }
    }
  }

  def execSql(sql:String, params:Any*){
    withConnection{con=>
      val ps = con.prepareStatement(sql)
      params.zipWithIndex.foreach{case (p,i) => ps.setObject(i+1, p)}
      ps.execute()
    }
  }

  private def createSchema(){
    withConnection {con =>
      printDdl {
        sql =>
          con.createStatement().execute(sql)
      }
    }
  }

  def createOrUpdateSchema(){
    transaction{
      val con = Session.currentSession.connection
      val meta = con.getMetaData
      val rs = meta.getTables(null, null, null, Array("TABLE"))
      val tableNames = Stream.continually(if(rs.next()) Some(rs.getString("TABLE_NAME")) else None).takeWhile(_.isDefined).flatten
      info("Existing tables: " + tableNames)
      if(tableNames.isEmpty){
        createSchema()
//        exec("setup" + ".sql")
        schemaInfo.insert(SchemaInfo("MAIN", version))
        info("Database schema created")
      }else{
        val dbVersion = tableNames.find(_.equalsIgnoreCase("schemaInfo")) match{
          case Some(_) => from(schemaInfo)(d=>select(d.version)).head
          case _ => 1
        }
        for(r<-dbVersion + 1 to version){
          info("upgrading db to version " +  r)
          exec("update_" + r + ".sql")
          update(schemaInfo)(d=>setAll(d.version := r))
        }
      }
    }
  }

}
