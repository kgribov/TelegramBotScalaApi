package com.kgribov.telegram.model

import com.kgribov.telegram.json.MessageJson
import org.scalatest.{FunSuite, Matchers}

class MessageJsonTest extends FunSuite with Matchers {

  test("model should return command from text") {
    val messageText = Some("/testcommand haha haha")
    val message = MessageJson(1, null, 1, null, rawText = messageText, chat = null)

    message.command should be (Some("testcommand"))
  }

  test("model should return only text, if command is not present") {
    val messageText = Some("just only text")
    val message = MessageJson(1, null, 1, null, rawText = messageText, chat = null)

    message.text should be ("just only text")
  }

  test("model should return text of command") {
    val messageText = Some("/testcommand your text here")
    val message = MessageJson(1, null, 1, null, rawText = messageText, chat = null)

    message.text should be ("your text here")
  }

  test("model should return command without text") {
    val messageText = Some("/testcommand")
    val message = MessageJson(1, null, 1, null, rawText = messageText, chat = null)

    message.command should be (Some("testcommand"))
  }
}
