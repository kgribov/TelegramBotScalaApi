package com.kgribov.telegram.bot.func
import com.kgribov.telegram.bot.func.bot.{CreateScheduleMessages, UpdateDialogsStatesFunc}
import com.kgribov.telegram.bot.loader.TelUpdate
import com.kgribov.telegram.bot.sender.TelBotReply
import com.kgribov.telegram.bot.state.BotState

class ProcessUpdateFunc(botFunctions: Seq[UserBotFunction],
                        filterBotFunctions: Seq[FilterBotFunction] = Seq.empty) extends UpdateBotFunction {

  private val createScheduleMessages = new CreateScheduleMessages
  private val processDialogs = new UpdateDialogsStatesFunc

  override def apply(state: BotState, update: TelUpdate): (BotState, TelBotReply) = {
    val (filteredUpdates, filterReplies) = filterUpdates(
      telUpdate = createScheduleMessages(state).merge(update),
      filterBotFunctions = filterBotFunctions
    )

    val (statesUpdates, botReply) = botFunctions
      .map(func => func.apply(filteredUpdates))
      .fold(emptyBotFunctionResult)(mergeStatesAndReplies)

    val (newBotStateWithDialogs, dialogsReplies) = processDialogs(state.merge(statesUpdates), filteredUpdates)

    val allReplies = botReply
      .merge(dialogsReplies)
      .merge(filterReplies)

    (newBotStateWithDialogs, allReplies)
  }

  private def filterUpdates(telUpdate: TelUpdate,
                            telBotReply: TelBotReply = TelBotReply(),
                            filterBotFunctions: Seq[FilterBotFunction]): (TelUpdate, TelBotReply) = {
    filterBotFunctions match {
      case function :: tail => {
        val (update, reply) = function(telUpdate)
        filterUpdates(update, reply, tail)
      }
      case Nil => (telUpdate, telBotReply)
    }
  }

  private def mergeStatesAndReplies(functionResult1: BotFunctionResult,
                                    functionResult2: BotFunctionResult): BotFunctionResult = {

    val (state1, reply1) = functionResult1
    val (state2, reply2) = functionResult2

    (state1.merge(state2), reply1.merge(reply2))
  }

  private def emptyBotFunctionResult: BotFunctionResult = (BotState(), TelBotReply())

  private type BotFunctionResult = (BotState, TelBotReply)
}
