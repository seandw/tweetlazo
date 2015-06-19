package org.cognoseed.tweetlazo

import org.scalatest.FunSuite

class UtilsTestSuite extends FunSuite {
  import Utils._

  test("stripDuplicateAdjacents strips duplicates") {
    assert("gooooooooooooooooool".stripDuplicateAdjacents === "gol")
  }

  test("stripDuplicateAdjacents doesn't choke on 1-length strings") {
    assert("?".stripDuplicateAdjacents === "?")
  }

  test("stripDuplicateAdjacents doesn't choke on 0-length strings") {
    assert("".stripDuplicateAdjacents === "")
  }

  test("stripDuplicateAdjacents can deal with no repetition") {
    assert("nothing you can do for this one".stripDuplicateAdjacents === "nothing you can do for this one")
  }

  test("stripPunctuation strips punctuation") {
    assert("¡GO-LA-ZO!".stripPunctuation === "GOLAZO")
  }

  test("stripPunctuation can deal with no punctuation") {
    assert("Mål".stripPunctuation === "Mål")
  }

  test("stripAccents strips accents") {
    assert("Mål!".stripAccents === "Mal!")
  }

  test("stripAccents can deal with no accents") {
    assert("welp, nothing you can do for this one".stripAccents === "welp, nothing you can do for this one")
  }

}
