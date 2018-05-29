package com.kgribov.telegram.bot.sender

import com.kgribov.telegram.bot.model._

case class TelBotReply(textMessages: Seq[BotTextReply] = Seq.empty,
                       keyboardReplies: Seq[BotKeyboardReply] = Seq.empty,
                       keyboardAlerts: Seq[BotKeyboardAlert] = Seq.empty) {

  def merge(telBotReply: TelBotReply): TelBotReply = {
    TelBotReply(
      textMessages = textMessages ++ telBotReply.textMessages,
      keyboardReplies = keyboardReplies ++ telBotReply.keyboardReplies,
      keyboardAlerts = keyboardAlerts ++ telBotReply.keyboardAlerts
    )
  }
}
