package com.kgribov.telegram.scheduler

case class CommandScheduleKey(command: String, chatId: Long) extends Serializable
