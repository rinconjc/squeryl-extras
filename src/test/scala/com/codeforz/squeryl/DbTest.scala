package com.codeforz.squeryl

import org.h2.jdbcx.JdbcDataSource
import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.H2Adapter

/**
 *
 */
object DbTest {
  def init(){
    val dataSource = new JdbcDataSource()
    dataSource.setURL("jdbc:h2:mem:test")
    SessionFactory.concreteFactory = Some(()=>Session.create(dataSource.getConnection, new H2Adapter))
  }
}
