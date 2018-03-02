package com.kgribov.telegram.sender

import com.kgribov.telegram.model.{KeyboardAlert, Message, MessageToSend}
import com.kgribov.telegram.parser._
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}
import scalaj.http.{Http, HttpRequest}

class MessageSender(apiKey: String,
                    retries: Int = 15,
                    sleepBetweenRetriesInMs: Int = 1000) extends LazyLogging {

  def sendMessages(messages: List[MessageToSend]): List[Message] = {
    messages.map(send)
  }

  def send(message: MessageToSend): Message = {
    logger.info(s"Going to send message: $message")
    val replyMarkup = message.replyKeyboard match {
      case Some(keyboard) => Seq(("reply_markup", keyboardToJson(keyboard)))
      case None => Seq[(String, String)]()
    }

    val params = Seq(
      ("chat_id", message.chatId.toString),
      ("text", message.text)
    ) ++ replyMarkup

    val request = Http(getSendMessageUrl).postForm(params)
    val response = requestForResponse(request)

    parseMessageResponse(response).toModel
  }

  private def requestForResponse(request: HttpRequest, tryCount: Int = 0): String = {
    val response = Try(request.asString.body)
    response match {
      case Success(body) => body
      case Failure(ex) => {
        logger.error(s"Unable to send request $request. Try count: $tryCount", ex)
        if (tryCount == retries) {
          throw new Exception("Max retries is reached for send request")
        } else {
          requestForResponse(request, tryCount + 1)
        }
      }
    }
  }

  def sendKeyboardAlert(keyboardAlert: KeyboardAlert): Unit = {
    Http(getAnswerCallbackUrl)
      .postForm(Seq(
        ("callback_query_id", keyboardAlert.messageId.toString),
        ("text", keyboardAlert.text),
        ("show_alert", keyboardAlert.showAlert.toString)
      )).asString
  }

  private def botHostName: String = s"https://api.telegram.org/bot$apiKey/"

  private def getSendMessageUrl = botHostName + "sendMessage"

  private def getAnswerCallbackUrl = botHostName + "answerCallbackQuery"
}
