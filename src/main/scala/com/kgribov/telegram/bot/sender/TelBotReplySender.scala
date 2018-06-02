package com.kgribov.telegram.bot.sender

import com.kgribov.telegram.bot.endpoints.TelegramEndpoints
import com.kgribov.telegram.bot.parser.keyboardToJson
import com.kgribov.telegram.bot.model.{BotKeyboardAlert, BotKeyboardReply, BotTextReply}
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}
import scalaj.http.{Http, HttpRequest}

class TelBotReplySender(apiKey: String, maxRetries: Int = 10) extends LazyLogging {

  private val SLEEP_BETWEEN_RETRIES_IN_SEC = 1

  def sendBotReply(botReply: TelBotReply): SendStatistic = {
    val sentAlerts = botReply
      .keyboardAlerts
      .map(alert => sendKeyboardAlert(alert))
      .count(_.isDefined)

    val sentKeyboards = botReply
      .keyboardReplies
      .map(reply => sendKeyboardReply(reply))
      .count(_.isDefined)

    val sentTextMessages = botReply
      .textMessages
      .map(message => sendTextReply(message))
      .count(_.isDefined)

    SendStatistic(
      sentKeyboardAlerts = sentAlerts,
      sentKeyboardReplies = sentKeyboards,
      sentTextReplies = sentTextMessages
    )
  }

  private def sendTextReply(textMessage: BotTextReply): Option[String] = {
    val request = Http(TelegramEndpoints.sendMessageUrl(apiKey))
      .postForm(Seq(
        ("chat_id", textMessage.chatId.toString),
        ("text", textMessage.text)
      ))
    sendRequest(request)
  }

  private def sendKeyboardReply(keyboardReply: BotKeyboardReply): Option[String] = {
    val request = Http(TelegramEndpoints.sendMessageUrl(apiKey))
      .postForm(Seq(
        ("chat_id", keyboardReply.chatId.toString),
        ("text", keyboardReply.text),
        ("reply_markup", keyboardToJson(keyboardReply.keyboard))
      ))
    sendRequest(request)
  }

  private def sendKeyboardAlert(keyboardAlert: BotKeyboardAlert): Option[String] = {
    val request = Http(TelegramEndpoints.answerCallbackUrl(apiKey))
      .postForm(Seq(
        ("callback_query_id", keyboardAlert.messageId.toString),
        ("text", keyboardAlert.text),
        ("show_alert", keyboardAlert.showAlert.toString)
      ))
    sendRequest(request)
  }

  private def sendRequest(httpRequest: HttpRequest, tryCount: Int = 0): Option[String] = {
    Try(httpRequest.timeout(connTimeoutMs = 10000, readTimeoutMs = 10000).asString) match {
      case Success(response) => Some(response.body)
      case Failure(ex) => {
        logger.error(s"Unable to send request: $httpRequest", ex)
        if (tryCount == maxRetries) {
          None
        } else {
          Thread.sleep(SLEEP_BETWEEN_RETRIES_IN_SEC * 1000)
          sendRequest(httpRequest, tryCount + 1)
        }
      }
    }
  }
}
