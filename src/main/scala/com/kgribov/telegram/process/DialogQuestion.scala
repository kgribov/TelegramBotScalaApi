package com.kgribov.telegram.process

import java.time.ZonedDateTime

import com.kgribov.telegram.model._
import com.kgribov.telegram.sender.MessageSender

trait DialogQuestion {

  def askQuestion(chatId: Int): String

  def isAsked: Boolean

  def processReply(reply: Message)

  def isDone(currentTime: ZonedDateTime = ZonedDateTime.now()): Option[Answer]

  def questionText: String
}

case class Answer(answers: List[Message]) {

  def isIgnored: Boolean = answers.isEmpty

  def isSimple: Boolean = answers.size == 1

  def simpleAnswer: Message = answers.head

  def allAnswers: List[Message] = answers
}

case class KeyboardDialogQuestion(messageSender: MessageSender,
                                  text: String,
                                  possibleAnswers: List[String],
                                  alertOnAnswer: String => String,
                                  expiredAt: Option[ZonedDateTime] = None) extends DialogQuestion {

  private var questionId: Option[String] = None
  private var answers = List[Message]()

  override def askQuestion(chatId: Int): String = {
    questionId = Some(messageSender.send(question(chatId)).id)
    questionId.get
  }

  override def isAsked: Boolean = questionId.isDefined

  override def processReply(reply: Message): Unit = {
    val isReplyToQuestion =
      questionId.isDefined &&
      reply.replyTo.exists(_.id == questionId.get) &&
      possibleAnswers.contains(reply.text)

    if (reply.replyToKeyboard && isReplyToQuestion) {
      if (answers.exists(ans => ans.from == reply.from)) {
        // already have answer from such user
        messageSender.sendKeyboardAlert(KeyboardAlert(reply.id, "You have already gave an answer", showAlert = true))
      } else {
        answers = reply :: answers
        messageSender.sendKeyboardAlert(KeyboardAlert(reply.id, alertOnAnswer(reply.text)))
      }
    }
  }

  override def isDone(currentTime: ZonedDateTime = ZonedDateTime.now()): Option[Answer] = {
    if (expiredAt.isDefined) {
      if (expiredAt.get.isBefore(currentTime)) {
        Some(Answer(answers))
      } else {
        None
      }

    } else {
      if (answers.isEmpty) {
        None
      } else {
        Some(Answer(answers))
      }
    }
  }

  override def questionText: String = text


  private def question(chatId: Int): MessageToSend = {
    MessageToSend(chatId, text, Some(Keyboard(possibleAnswers)))
  }
}

case class TextDialogQuestion(messageSender: MessageSender,
                              text: String,
                              alertOnAnswer: Option[String => String] = None) extends DialogQuestion {

  private var questionId: Option[String] = None
  private var answer: Option[Answer] = None

  override def isAsked: Boolean = questionId.isDefined

  override def askQuestion(chatId: Int): String = {
    questionId = Some(messageSender.send(question(chatId)).id)
    questionId.get
  }

  override def processReply(reply: Message): Unit = {
    if (answer.isEmpty) {
      answer = Some(Answer(List(reply)))
    } else {
      // user already gave the answer
    }
  }

  override def isDone(currentTime: ZonedDateTime = ZonedDateTime.now()): Option[Answer] = {
    answer
  }

  override def questionText: String = text

  private def question(chatId: Int): MessageToSend = {
    MessageToSend(chatId, text)
  }
}

case class DummyDialogQuestion(dummyText: String, messageSender: MessageSender) extends DialogQuestion {

  private var questionId: Option[String] = None

  override def askQuestion(chatId: Int): String = {
    questionId = Some(messageSender.send(MessageToSend(chatId, dummyText)).id)
    questionId.get
  }

  override def processReply(reply: Message): Unit = {}

  override def isDone(currentTime: ZonedDateTime): Option[Answer] = Some(Answer(List()))

  override def questionText: String = "Dummy"

  override def isAsked = questionId.isDefined
}
