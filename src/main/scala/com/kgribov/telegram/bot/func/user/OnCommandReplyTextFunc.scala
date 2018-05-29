package com.kgribov.telegram.bot.func.user

import com.kgribov.telegram.bot.func.UserBotFunction
import com.kgribov.telegram.bot.loader.TelUpdate
import com.kgribov.telegram.bot.model.{BotTextReply, CommandMessage}
import com.kgribov.telegram.bot.sender.TelBotReply
import com.kgribov.telegram.bot.state.BotState

class OnCommandReplyTextFunc(command: String,
                             processMessage: CommandMessage => Option[String]) extends UserBotFunction {

  override def apply(update: TelUpdate): (BotState, TelBotReply) = {
    val botTextReplies = update
      .commandMessages
      .filter(_.command == command)
      .map(message => (message, processMessage(message)))
      .filter{ case (_, reply) => reply.isDefined }
      .map{ case (inputMessage, reply) => BotTextReply(inputMessage.chat.id, reply.get) }

    val botReply = TelBotReply(textMessages = botTextReplies)

    (BotState(), botReply)
  }
}
