package com.kgribov.telegram.bot.parser

import org.scalatest.{FunSuite, Matchers}

import scala.io.Source

class ParserTest extends FunSuite with Matchers {

  test("parser should read response from telegram correctly") {
    val response = Source.fromResource("response_from_user.json").mkString

    val updates = parseUpdates(response)

    updates should have size 2

    updates.head.id should be(776799926)
  }

  test("parser should return none if field is missing") {
    val response = Source.fromResource("response_from_user.json").mkString

    val updates = parseUpdates(response)

    updates should have size 2

    updates.head.message.get.chat.title should be (None)
  }

  test("parser should return some if field is present") {
    val response = Source.fromResource("response_from_user.json").mkString

    val updates = parseUpdates(response)

    updates should have size 2

    updates.head.message.get.chat.description should be (Some("lol"))
  }
}
