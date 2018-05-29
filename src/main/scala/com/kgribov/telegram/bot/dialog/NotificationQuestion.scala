package com.kgribov.telegram.bot.dialog
import java.time.LocalDateTime

import com.kgribov.telegram.bot.loader.TelUpdate
import com.kgribov.telegram.bot.model.BotTextReply
import com.kgribov.telegram.bot.sender.TelBotReply

class NotificationQuestion(notificationText: String) extends DialogQuestion {

  override def askQuestion(chatId: String): TelBotReply = TelBotReply(
    textMessages = Seq(BotTextReply(chatId, notificationText))
  )

  override def getAnswers(update: TelUpdate): Seq[Answer] = Seq.empty

  override def rejectAnswer(answer: Answer, alreadyAnswered: Boolean): Option[TelBotReply] = None

  override def notifyOnAnswer(answer: Answer): Option[TelBotReply] = None

  override def isDone(answers: Seq[Answer],
                      startedAt: LocalDateTime,
                      now: LocalDateTime): Boolean = true

  override def askOnlyOneUser: Boolean = false

  def questionText: String = notificationText
}
