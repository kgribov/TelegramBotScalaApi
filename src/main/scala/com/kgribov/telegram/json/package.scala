package com.kgribov.telegram

import java.time.{Instant, ZoneId, ZonedDateTime}

import com.fasterxml.jackson.annotation.JsonProperty
import com.kgribov.telegram.model._

package object json {

  case class UpdateJson(@JsonProperty(required = true, value = "update_id") id: Long,
                        @JsonProperty(value = "message") message: Option[MessageJson],
                        @JsonProperty(value = "callback_query") callbackQuery: Option[CallbackQueryJson]) {

    def toModel: Update = {
      val messageModel = message.map(_.toModel)
      val callbackModel = callbackQuery.map(_.toMessageModel)

      Update(id, messageModel.orElse(callbackModel))
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

  case class ChatJson(@JsonProperty(required = true, value = "id") id: Long,
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
                               @JsonProperty(value = "data") data: String) {

    def toMessageModel: Message = {
      Message(id, from.toModel, None, Some(message.toModel), true, message.getDate, message.toModel.chat, data)
    }
  }

  case class MessageJson(@JsonProperty(required = true, value = "message_id") id: Long,
                         @JsonProperty(required = true, value = "from") from: UserJson,
                         @JsonProperty(required = true, value = "date") date: Long,
                         @JsonProperty(value = "reply_to_message") replyTo: Option[MessageJson],
                         @JsonProperty(required = true, value = "chat") chat: ChatJson,
                         @JsonProperty(value = "text") rawText: Option[String]) {

    def getDate: ZonedDateTime = Instant.ofEpochSecond(date).atZone(ZoneId.of("Z"))

    def command: Option[String] = {
      if (splitText._1.isEmpty) {
        None
      } else {
        Some(splitText._1)
      }
    }

    def text: String = splitText._2

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

    def toModel: Message = {
      Message(id.toString, from.toModel, command, replyTo.map(_.toModel), false, getDate, chat.toModel, text)
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
