package com.codeforz.squeryl

import org.specs2.mutable.SpecificationWithJUnit

/**
 *
 */
class SchemaVersioningTest extends SpecificationWithJUnit {
  DbTest.init()
  "create a new DB" in {
    TestModel.createOrUpdateSchema()
  }

}

