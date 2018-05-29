package com.kgribov.telegram.bot

import java.util.concurrent.atomic.AtomicBoolean

import com.kgribov.telegram.bot.func.{UserBotFunction, FilterBotFunction, ProcessUpdateFunc}
import com.kgribov.telegram.bot.loader.TelUpdateLoader
import com.kgribov.telegram.bot.sender.TelBotReplySender
import com.kgribov.telegram.bot.state.StateStore

class BotRunner(apiKey: String,
                userBotFunctions: Seq[UserBotFunction],
                filterBotFunctions: Seq[FilterBotFunction] = Seq.empty,
                stateStore: StateStore) {

  val sender = new TelBotReplySender(apiKey)
  val loader = new TelUpdateLoader(apiKey)

  def run(): Unit = {
    val processUpdateFunc = new ProcessUpdateFunc(
      botFunctions = userBotFunctions,
      filterBotFunctions = filterBotFunctions
    )
    val bot = new Bot(
      processUpdate = processUpdateFunc,
      loadUpdateFromOffset = loader.loadUpdateFromOffset,
      sendBotReply = sender.sendBotReply,
      saveState = stateStore.storeState,
      stopBot = new StopBotOnSignal
    )

    bot.processFromOffset()
  }

  private class StopBotOnSignal extends (Long => Boolean) {
    private val stop = new AtomicBoolean(false)
    sys.addShutdownHook(stop.set(true))

    override def apply(offset: Long): Boolean = {
      stop.get()
    }
  }
}
