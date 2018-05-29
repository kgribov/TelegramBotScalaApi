package com.kgribov.telegram.bot

import com.kgribov.telegram.bot.loader.TelUpdate
import com.kgribov.telegram.bot.func._
import com.kgribov.telegram.bot.sender.{SendStatistic, TelBotReply}
import com.kgribov.telegram.bot.state.BotState
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

class Bot(processUpdate: ProcessUpdateFunc,
          loadUpdateFromOffset: Long => (TelUpdate, Long),
          sendBotReply: TelBotReply => SendStatistic,
          saveState: (Long, BotState) => Long,
          stopBot: Long => Boolean = _ => false) extends LazyLogging {

  def processFromOffset(startBotFromOffset: Long = 0,
                        botState: BotState = BotState()): Unit = {

    val (update, nextOffset) = loadUpdatesSafely(startBotFromOffset)

    logger.info(
      s"Going to process ${update.textMessages.size} text messages, " +
        s"${update.commandMessages.size} command messages, " +
        s"${update.keyboardReplies.size} keyboard replies"
    )

    val (processedState, replies) = processUpdate(botState, update)

    sendBotReply(replies)

    saveState(nextOffset, processedState)

    logger.info(s"Updated state contains ${processedState.dialogsStates.size} dialogs, " +
      s"${processedState.commandsSchedules.size} scheduled commands")

    if (!stopBot(nextOffset)) {
      processFromOffset(nextOffset, processedState)
    } else {
      logger.info("Bot was stopped by stop-function")
    }
  }

  private def loadUpdatesSafely(fromOffset: Long): (TelUpdate, Long) = {
    Try(loadUpdateFromOffset(fromOffset)) match {

      case Success((update, nextOffset)) => (update, nextOffset)

      case Failure(ex) => {
        logger.error(s"Unable to load new updates from offset=$fromOffset", ex)
        (TelUpdate(), fromOffset)
      }
    }
  }
}
