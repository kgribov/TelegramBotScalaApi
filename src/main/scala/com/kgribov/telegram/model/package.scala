package com.kgribov.telegram

import java.time.ZonedDateTime

package object model {

  val BOT_USER = User(0, isBot = true, "current_bot", None, None)

  case class Update(id: Long, message: Option[Message])

  case class User(id: Long, isBot: Boolean, firstName: String, lastName: Option[String], username: Option[String])

  case class Chat(id: Long, title: Option[String], description: Option[String], chatType: String)

  case class MessageToSend(chatId: Long, text: String, replyKeyboard: Option[Keyboard] = None)

  trait ToSendMessage { def toSend(chatId: Long): MessageToSend }

  case class QuizMessage(question: String, options: List[String]) extends ToSendMessage {
    def toSend(chatId: Long): MessageToSend = {
      MessageToSend(chatId, question, Some(Keyboard(options)))
    }
  }

  case class Message(id: String,
                     from: User,
                     command: Option[String] = None,
                     replyTo: Option[Message] = None,
                     replyToKeyboard: Boolean = false,
                     date: ZonedDateTime,
                     chat: Chat,
                     text: String)

  case class KeyboardAlert(messageId: String, text: String = "", showAlert: Boolean = false)

  case class Keyboard(buttons: List[String])
}
