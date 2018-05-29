package com.kgribov.telegram.bot.loader

import com.kgribov.telegram.bot.model._

case class TelUpdate(textMessages: Seq[TextMessage] = Seq.empty,
                     commandMessages: Seq[CommandMessage] = Seq.empty,
                     keyboardReplies: Seq[KeyboardReply] = Seq.empty) {

  def updateInChat(chatId: String): TelUpdate = {
    TelUpdate(
      textMessages = textMessages.filter(_.chat.id == chatId),
      commandMessages = commandMessages.filter(_.chat.id == chatId),
      keyboardReplies = keyboardReplies.filter(_.keyboardMes.exists(message => message.chat.id == chatId))
    )
  }

  def updateFromUser(userId: Long): TelUpdate = {
    TelUpdate(
      textMessages = textMessages.filter(_.from.id == userId),
      commandMessages = commandMessages.filter(_.from.id == userId),
      keyboardReplies = keyboardReplies.filter(_.from.id == userId)
    )
  }

  def merge(telUpdate: TelUpdate): TelUpdate = {
    TelUpdate(
      textMessages = textMessages ++ telUpdate.textMessages,
      commandMessages = commandMessages ++ telUpdate.commandMessages,
      keyboardReplies = keyboardReplies ++ telUpdate.keyboardReplies
    )
  }
}
