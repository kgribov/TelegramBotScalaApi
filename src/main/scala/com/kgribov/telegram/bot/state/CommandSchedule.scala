package com.kgribov.telegram.bot.state

import java.time.LocalDateTime

import com.kgribov.telegram.bot.model.Chat

case class CommandSchedule(commandName: String,
                           chat: Chat,
                           cronExpression: String,
                           lastRun: Option[LocalDateTime] = None)
