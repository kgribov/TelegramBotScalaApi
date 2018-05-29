package com.kgribov.telegram.bot.scheduler

import java.time.{LocalDateTime, ZonedDateTime}

import com.kgribov.telegram.bot.loader.TelUpdate
import com.kgribov.telegram.bot.model._
import com.kgribov.telegram.bot.state.CommandSchedule
import cron4s._
import cron4s.lib.javatime._

import scala.util.Random

object CommandsScheduler {

  def createMessagesOnSchedule(commandSchedule: CommandSchedule,
                               now: ZonedDateTime = ZonedDateTime.now()): TelUpdate = {

    val messages = if (needToExec(commandSchedule, now.toLocalDateTime)) {
      Seq(CommandMessage(
        id = Random.nextString(10),
        from = BOT_USER,
        command = commandSchedule.commandName,
        date = now,
        chat = commandSchedule.chat
      ))
    } else {
      Seq.empty
    }
    TelUpdate(commandMessages = messages)
  }

  private def needToExec(scheduleCron: CommandSchedule, now: LocalDateTime): Boolean = {
    val Right(cron) = Cron(scheduleCron.cronExpression)
    val prevCronRun = cron.prev(now)

    val lastRun = scheduleCron.lastRun.getOrElse(LocalDateTime.MIN)

    prevCronRun.exists(_.isAfter(lastRun))
  }
}
