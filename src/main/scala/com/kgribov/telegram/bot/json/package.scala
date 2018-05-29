package com.kgribov.telegram.bot

import java.time.{Instant, ZoneId, ZonedDateTime}

import com.fasterxml.jackson.annotation.JsonProperty
import com.kgribov.telegram.bot.loader.TelUpdate
import com.kgribov.telegram.bot.model._

package object json {

  case class UpdateJson(@JsonProperty(required = true, value = "update_id") id: Long,
                        @JsonProperty(value = "message") message: Option[MessageJson],
                        @JsonProperty(value = "callback_query") callbackQuery: Option[CallbackQueryJson]) {

    def toModel: (TelUpdate, Long) = {
      val textMessage = message.flatMap(_.toTextMessage)
      val commandMessage = message.flatMap(_.toCommandMessage)
      val keyboardReply = callbackQuery.map(_.toModel)

      val update = TelUpdate(
        textMessages = textMessage.toSeq,
        commandMessages = commandMessage.toSeq,
        keyboardReplies = keyboardReply.toSeq
      )
      (update, id)
    }
  }

  case class UserJson(@JsonProperty(required = true, value = "id") id: Long,
                      @JsonProperty(required = true, value = "is_bot") isBot: Boolean,
                      @JsonProperty(required = true, value = "first_name") firstName: String,
                      @JsonProperty(value = "last_name") lastName: Option[String],
                      @JsonProperty(value = "username") username: Option[String]) {

    def toModel: User = {
      User(id, isBot, firstName, lastName, username)
    }
  }

  case class ChatMemberJson(@JsonProperty(required = true, value = "user") user: UserJson,
                            @JsonProperty(required = true, value = "status") status: String) {
    def toModel: User = {
      user.toModel
    }
  }

  case class ChatJson(@JsonProperty(required = true, value = "id") id: String,
                      @JsonProperty(value = "title") title: Option[String],
                      @JsonProperty(value = "description") description: Option[String],
                      @JsonProperty(required = true, value = "type") chatType: String) {

    def toModel: Chat = {
      Chat(id, title, description, chatType)
    }
  }

  case class CallbackQueryJson(@JsonProperty(required = true, value = "id") id: String,
                               @JsonProperty(required = true, value = "from") from: UserJson,
                               @JsonProperty(value = "message") message: MessageJson,
                               @JsonProperty(value = "data") data: String,
                                @JsonProperty(value = "chat_instance") chatId: String) {

    def toModel: KeyboardReply = {
      KeyboardReply(id, from.toModel, data, message.toTextMessage, None, chatId)
    }
  }

  case class MessageJson(@JsonProperty(required = true, value = "message_id") id: Long,
                         @JsonProperty(required = true, value = "from") from: UserJson,
                         @JsonProperty(required = true, value = "date") date: Long,
                         @JsonProperty(value = "reply_to_message") replyTo: Option[MessageJson],
                         @JsonProperty(required = true, value = "chat") chat: ChatJson,
                         @JsonProperty(value = "text") rawText: Option[String]) {

    private def getDate: ZonedDateTime = Instant.ofEpochSecond(date).atZone(ZoneId.of("Z"))

    private def command: Option[String] = {
      if (splitText._1.isEmpty) {
        None
      } else {
        Some(splitText._1)
      }
    }

    private def text: String = splitText._2

    private def splitText: (String, String) = {
      if (rawText.isEmpty) {
        ("", "")

      } else if (rawText.isDefined && rawText.get.startsWith("/")) {
        val commandRegex  = "/(\\w+)(\\s?)(.*)".r
        val commandRegex(command, _, messageText) = rawText.get
        (command, messageText)
      } else {
        ("", rawText.getOrElse(""))
      }
    }

    def toTextMessage: Option[TextMessage] = {
      if (command.isEmpty) {
        Some(TextMessage(id.toString, from.toModel, replyTo.flatMap(_.toTextMessage), getDate, chat.toModel, text))
      } else {
        None
      }
    }

    def toCommandMessage: Option[CommandMessage] = {
      command.map(commandText => CommandMessage(id.toString, from.toModel, commandText, getDate, chat.toModel))
    }
  }

  case class InlineKeyboardMarkup(@JsonProperty(required = true, value = "inline_keyboard") keyboard: Array[Array[InlineKeyboardButton]])

  object InlineKeyboardMarkup {
    def fromKeyboard(keyboard: Keyboard): InlineKeyboardMarkup = {
      InlineKeyboardMarkup(keyboard.buttons.map(
        button => Array(InlineKeyboardButton(button, Some(button)))
      ).toArray)
    }
  }

  case class InlineKeyboardButton(@JsonProperty(required = true, value = "text") text: String,
                                  @JsonProperty(value = "callback_data") callbackData: Option[String] = None)
}
