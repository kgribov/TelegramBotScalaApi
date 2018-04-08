package com.kgribov.telegram.sender

import com.kgribov.telegram.endpoints.TelegramEndpoints
import com.kgribov.telegram.http._
import com.kgribov.telegram.model.{KeyboardAlert, Message, MessageToSend}
import com.kgribov.telegram.parser._
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}
import scalaj.http.Http

class MessageSender(apiKey: String,
                    retries: Int = 15,
                    sleepBetweenRetriesInMs: Int = 2000) extends LazyLogging {

  def sendMessages(messages: List[MessageToSend]): List[Message] = {
    messages.map(sendMessageSafely).flatten
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

    val request = Http(TelegramEndpoints.sendMessageUrl(apiKey))
      .timeout(connTimeoutMs = 10000, readTimeoutMs = 10000)
      .postForm(params)
    val response = requestForTextResponse(request, retries)

    parseMessageResponse(response).toModel
  }

  def sendKeyboardAlert(keyboardAlert: KeyboardAlert): Unit = {
    Http(TelegramEndpoints.answerCallbackUrl(apiKey))
      .postForm(Seq(
        ("callback_query_id", keyboardAlert.messageId.toString),
        ("text", keyboardAlert.text),
        ("show_alert", keyboardAlert.showAlert.toString)
      ))
      .timeout(connTimeoutMs = 10000, readTimeoutMs = 10000)
      .asString
  }

  private def sendMessageSafely(message: MessageToSend): Option[Message] = {
    val sendMessage = Try(send(message))
    sendMessage match {
      case Success(message) => Some(message)
      case Failure(ex) => {
        logger.error(s"Unable to send message: $message", ex)
        None
      }
    }
  }
}
