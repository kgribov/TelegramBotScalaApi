package com.kgribov.telegram.sender

import com.kgribov.telegram.endpoints.TelegramEndpoints
import com.kgribov.telegram.http._
import com.kgribov.telegram.model.{KeyboardAlert, Message, MessageToSend}
import com.kgribov.telegram.parser._
import com.typesafe.scalalogging.LazyLogging

import scalaj.http.Http

class MessageSender(apiKey: String,
                    retries: Int = 15,
                    sleepBetweenRetriesInMs: Int = 1000) extends LazyLogging {

  def sendMessages(messages: List[MessageToSend]): List[Message] = {
    messages.map(send)
  }

  def send(message: MessageToSend): Message = {
    logger.debug(s"Going to send message: $message")
    val replyMarkup = message.replyKeyboard match {
      case Some(keyboard) => Seq(("reply_markup", keyboardToJson(keyboard)))
      case None => Seq[(String, String)]()
    }

    val params = Seq(
      ("chat_id", message.chatId.toString),
      ("text", message.text)
    ) ++ replyMarkup

    val request = Http(TelegramEndpoints.sendMessageUrl(apiKey)).postForm(params)
    val response = requestForResponse(request, retries)

    parseMessageResponse(response).toModel
  }

  def sendKeyboardAlert(keyboardAlert: KeyboardAlert): Unit = {
    Http(TelegramEndpoints.answerCallbackUrl(apiKey))
      .postForm(Seq(
        ("callback_query_id", keyboardAlert.messageId.toString),
        ("text", keyboardAlert.text),
        ("show_alert", keyboardAlert.showAlert.toString)
      )).asString
  }
}
