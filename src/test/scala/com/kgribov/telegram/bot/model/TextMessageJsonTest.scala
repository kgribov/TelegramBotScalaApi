package com.kgribov.telegram.bot.model

import com.kgribov.telegram.bot.json.{ChatJson, MessageJson, UserJson}
import org.scalatest.{FunSuite, Matchers}

class TextMessageJsonTest extends FunSuite with Matchers {

  private val testUser = UserJson(1, isBot = false, "bot", None, None)
  private val testChat = ChatJson("1", None, None, "private")

  test("model should return command from text") {
    val messageText = Some("/testcommand haha haha")
    val message = MessageJson(1, testUser, 1, None, rawText = messageText, chat = testChat)

    message.toCommandMessage.get.command should be ("testcommand")
  }

  test("model should return only text, if command is not present") {
    val messageText = Some("just only text")
    val message = MessageJson(1, testUser, 1, None, rawText = messageText, chat = testChat)

    message.toTextMessage.get.text should be ("just only text")
  }

  test("model should return command without text") {
    val messageText = Some("/testcommand")
    val message = MessageJson(1, testUser, 1, None, rawText = messageText, chat = testChat)

    message.toCommandMessage.get.command should be ("testcommand")
  }
}
