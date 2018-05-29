package com.kgribov.telegram.bot.dialog

import java.time.LocalDateTime

import com.kgribov.telegram.bot.loader.TelUpdate
import com.kgribov.telegram.bot.model.BotTextReply
import com.kgribov.telegram.bot.sender.TelBotReply

import scala.concurrent.duration.Duration

class TextQuestion(question: String,
                   questionTTL: Option[Duration] = None,
                   rejectAnswer: String => Option[String] = _ => None,
                   withOnlyOneUser: Boolean = false) extends DialogQuestion {

  override def askQuestion(chatId: String): TelBotReply = {
    TelBotReply(textMessages = Seq(BotTextReply(chatId, question)))
  }

  override def getAnswers(update: TelUpdate): Seq[Answer] = {
    update.textMessages.map(
      textMessage => Answer(textMessage.id, textMessage.from, textMessage.chat.id, textMessage.text)
    )
  }

  override def rejectAnswer(answer: Answer, alreadyAnswered: Boolean): Option[TelBotReply] = {
    rejectAnswer(answer.answer).map(textAnswer => TelBotReply(textMessages = Seq(
      BotTextReply(answer.fromChatId, textAnswer)
    )))
  }

  override def notifyOnAnswer(answer: Answer): Option[TelBotReply] = None

  override def isDone(answers: Seq[Answer], startedAt: LocalDateTime, now: LocalDateTime): Boolean = {
    val haveAnswers = answers.nonEmpty
    val expired = questionTTL.map(ttl => startedAt.plusSeconds(ttl.toSeconds).isBefore(now))
    expired.getOrElse(haveAnswers)
  }

  override def askOnlyOneUser: Boolean = withOnlyOneUser

  def questionText: String = question
}
