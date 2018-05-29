package com.kgribov.telegram.bot.func.bot

import com.kgribov.telegram.bot.func.AskBotFunction
import com.kgribov.telegram.bot.loader.TelUpdate
import com.kgribov.telegram.bot.state.BotState
import com.kgribov.telegram.bot.scheduler.CommandsScheduler

class CreateScheduleMessages extends AskBotFunction {

  override def apply(botState: BotState): TelUpdate = {
    val messagesToSend = botState
      .commandsSchedules
      .map(CommandsScheduler.createMessagesOnSchedule(_))

    if (messagesToSend.isEmpty) {
      TelUpdate()
    } else {
      messagesToSend.reduce((update1, update2) => update1.merge(update2))
    }
  }
}
