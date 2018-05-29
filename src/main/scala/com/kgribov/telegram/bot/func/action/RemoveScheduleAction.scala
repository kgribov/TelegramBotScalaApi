package com.kgribov.telegram.bot.func.action

import com.kgribov.telegram.bot.func.UserStateAction
import com.kgribov.telegram.bot.model.Chat
import com.kgribov.telegram.bot.state.{BotState, CommandSchedule}

class RemoveScheduleAction(commandName: String, cronExpression: String) extends UserStateAction {

  override def apply(chatId: Chat, state: BotState): BotState = {
    state.copy(
      commandsSchedules = state.commandsSchedules.filterNot(isCurrentSchedule)
    )
  }

  private def isCurrentSchedule(schedule: CommandSchedule): Boolean =
    schedule.commandName == commandName && schedule.cronExpression == cronExpression
}
