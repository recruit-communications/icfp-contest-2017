package com.kenkoooo.sugoi

import com.kenkoooo.sugoi.SugoiDealer.mapper
import org.scalatest.{FunSuite, Matchers}

class SugoiDealerSuite extends FunSuite with Matchers {
  test("") {
    val map = mapper.readValue[LambdaMap](getClass.getClassLoader.getResource("sample.json").toURI.toURL, classOf[LambdaMap])
    val punterProgram = new PunterProgram("./punter", 1)
    punterProgram.putCommand("{\"punter\":0,\"punters\":2,\"map\":{\"sites\":[{\"id\":4},{\"id\":1},{\"id\":3},{\"id\":6},{\"id\":5},{\"id\":0},{\"id\":7},{\"id\":2}],\"rivers\":[{\"source\":3,\"target\":4},{\"source\":0,\"target\":1},{\"source\":2,\"target\":3},{\"source\":1,\"target\":3},{\"source\":5,\"target\":6},{\"source\":4,\"target\":5},{\"source\":3,\"target\":5},{\"source\":6,\"target\":7},{\"source\":5,\"target\":7},{\"source\":1,\"target\":7},{\"source\":0,\"target\":7},{\"source\":1,\"target\":2}],\"mines\":[1,5]}}", 10)
  }

}
