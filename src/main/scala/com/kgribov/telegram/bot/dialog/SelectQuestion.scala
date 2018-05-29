package com.kgribov.telegram.bot.dialog
import java.time.LocalDateTime

import com.kgribov.telegram.bot.loader.TelUpdate
import com.kgribov.telegram.bot.model.{BotKeyboardAlert, BotKeyboardReply, Keyboard}
import com.kgribov.telegram.bot.sender.TelBotReply

import scala.concurrent.duration.Duration

class SelectQuestion(question: String,
                     possibleAnswers: Seq[String],
                     sendNotification: Answer => Option[String],
                     alreadyAnsweredNotification: String,
                     questionTTL: Option[Duration] = None,
                     withOnlyOneUser: Boolean = false) extends DialogQuestion {

  override def askQuestion(chatId: String): TelBotReply = {
    TelBotReply(
      keyboardReplies = Seq(BotKeyboardReply(chatId, question, Keyboard(possibleAnswers.toList)))
    )
  }

  override def getAnswers(update: TelUpdate): Seq[Answer] = {
    update
      .keyboardReplies
      .map(reply => Answer(reply.id, reply.from, reply.chatId, reply.buttonText))
  }

  override def rejectAnswer(answer: Answer, alreadyAnswered: Boolean): Option[TelBotReply] = {
    if (alreadyAnswered) {
      val keyboardAlert = BotKeyboardAlert(answer.messageId, alreadyAnsweredNotification, showAlert = true)
      Some(TelBotReply(keyboardAlerts = Seq(keyboardAlert)))
    } else {
      None
    }
  }

  override def notifyOnAnswer(answer: Answer): Option[TelBotReply] = {
    val keyboardAlert = sendNotification(answer).map(notifyText => BotKeyboardAlert(answer.messageId, notifyText, showAlert = true))
    keyboardAlert.map(
      alert => TelBotReply(keyboardAlerts = Seq(alert))
    )
  }

  override def isDone(answers: Seq[Answer], startedAt: LocalDateTime, now: LocalDateTime): Boolean = {
    val haveAnswers = answers.nonEmpty
    val expired = questionTTL.map(ttl => startedAt.plusSeconds(ttl.toSeconds).isBefore(now))
    expired.getOrElse(haveAnswers)
  }

  override def askOnlyOneUser: Boolean = withOnlyOneUser

  def questionText: String = question
}
