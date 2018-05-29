package com.kgribov.telegram.bot.state

case class BotState(dialogsStates: Seq[DialogState] = Seq.empty,
                    commandsSchedules: Seq[CommandSchedule] = Seq.empty) {

  def merge(state: BotState): BotState = {
    BotState(
      dialogsStates = dialogsStates ++ state.dialogsStates,
      commandsSchedules = commandsSchedules ++ state.commandsSchedules
    )
  }
}
