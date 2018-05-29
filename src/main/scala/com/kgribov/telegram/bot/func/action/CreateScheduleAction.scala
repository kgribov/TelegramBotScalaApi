package com.kgribov.telegram.bot.func.action

import com.kgribov.telegram.bot.func.UserStateAction
import com.kgribov.telegram.bot.model.Chat
import com.kgribov.telegram.bot.state.{BotState, CommandSchedule}

class CreateScheduleAction(command: String, cronExpression: String) extends UserStateAction {

  override def apply(chat: Chat, state: BotState): BotState = {
    state.copy(
      commandsSchedules = state.commandsSchedules :+ CommandSchedule(command, chat, cronExpression)
    )
  }
}
