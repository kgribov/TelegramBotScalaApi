package com.kgribov.telegram.bot.dialog

import java.time.LocalDateTime

import com.kgribov.telegram.bot.loader.TelUpdate
import com.kgribov.telegram.bot.sender.TelBotReply

trait DialogQuestion {

  def askQuestion(chatId: String): TelBotReply

  def getAnswers(update: TelUpdate): Seq[Answer]

  def rejectAnswer(answer: Answer, alreadyAnswered: Boolean): Option[TelBotReply]

  def notifyOnAnswer(answer: Answer): Option[TelBotReply]

  def isDone(answers: Seq[Answer], startedAt: LocalDateTime, now: LocalDateTime = LocalDateTime.now()): Boolean

  def askOnlyOneUser: Boolean

  def questionText: String
}
