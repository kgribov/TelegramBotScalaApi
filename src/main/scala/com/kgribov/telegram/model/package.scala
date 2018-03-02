package com.kgribov.telegram

import java.time.ZonedDateTime

package object model {

  case class Update(id: Int, message: Message)

  case class User(id: Int, isBot: Boolean, firstName: String, lastName: Option[String], username: Option[String])

  case class Chat(id: Int, title: Option[String], description: Option[String], chatType: String)

  case class MessageToSend(chatId: Int, text: String, replyKeyboard: Option[Keyboard] = None)

  trait ToSendMessage { def toSend(chatId: Int): MessageToSend }

  case class QuizMessage(question: String, options: List[String]) extends ToSendMessage {
    def toSend(chatId: Int): MessageToSend = {
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
