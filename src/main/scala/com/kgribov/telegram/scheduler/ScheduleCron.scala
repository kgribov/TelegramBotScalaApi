package com.kgribov.telegram.scheduler

import java.time.ZonedDateTime

case class ScheduleCron(cron: String, lastRun: ZonedDateTime) extends Serializable
