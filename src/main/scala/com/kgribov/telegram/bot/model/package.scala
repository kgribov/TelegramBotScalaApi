package com.kgribov.telegram.bot

import java.time.ZonedDateTime

package object model {

  val BOT_USER = User(0, isBot = true, "current_bot", None, None)

  case class Update(id: Long,
                    textMessage: Option[TextMessage],
                    keyboardReply: Option[KeyboardReply])

  case class User(id: Long,
                  isBot: Boolean,
                  firstName: String,
                  lastName: Option[String],
                  username: Option[String])

  case class Chat(id: String,
                  title: Option[String],
                  description: Option[String],
                  chatType: String)


  case class TextMessage(id: String,
                         from: User,
                         replyTo: Option[TextMessage] = None,
                         date: ZonedDateTime,
                         chat: Chat,
                         text: String)

  case class CommandMessage(id: String,
                            from: User,
                            command: String,
                            date: ZonedDateTime,
                            chat: Chat)

  case class KeyboardReply(id: String,
                           from: User,
                           buttonText: String,
                           keyboardMes: Option[TextMessage],
                           inlineMesId: Option[String],
                           chatId: String)

  case class BotTextReply(chatId: String,
                          text: String)

  case class BotKeyboardReply(chatId: String,
                              text: String,
                              keyboard: Keyboard)

  case class BotKeyboardAlert(messageId: String,
                              text: String = "",
                              showAlert: Boolean = false)

  case class Keyboard(buttons: List[String])
}
