package com.kgribov.telegram.bot

import com.kgribov.telegram.bot.loader.TelUpdate
import com.kgribov.telegram.bot.model.Chat
import com.kgribov.telegram.bot.sender.TelBotReply
import com.kgribov.telegram.bot.state.BotState

package object func {

  trait UserBotFunction extends (TelUpdate => (BotState, TelBotReply))

  trait AskBotFunction extends (BotState => TelUpdate)

  trait UpdateBotFunction extends ((BotState, TelUpdate) => (BotState, TelBotReply))

  trait FilterBotFunction extends (TelUpdate => (TelUpdate, TelBotReply))

  trait UserStateAction extends ((Chat, BotState) => BotState)
}
