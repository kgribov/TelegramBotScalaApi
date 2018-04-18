package com.kgribov.telegram.scheduler

import java.time.{ZoneId, ZonedDateTime}

import com.kgribov.telegram.model.BOT_USER
import org.scalatest.{FunSuite, Matchers}

class CommandsSchedulerTest extends FunSuite with Matchers {

  val now = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault())
  val testCommand = "test"
  val testChatId = 1L
  val testCron = "0 */5 * ? * *"

  test("should return new commands if time is come") {
    val scheduler = new CommandsScheduler()

    val scheduleTime = now.minusMinutes(6)
    scheduler.scheduleCommand(testCommand, testCron, testChatId, scheduleTime)

    val messages = scheduler.retrieveMessagesToSend(now)

    messages should have size 1

    val message = messages.head
    message.from should be (BOT_USER)
    message.chat.id should be (testChatId)
  }

  test("should return no new commands on second retrieve") {
    val scheduler = new CommandsScheduler()

    val scheduleTime = now.minusMinutes(6)
    scheduler.scheduleCommand(testCommand, testCron, testChatId, scheduleTime)

    val messages = scheduler.retrieveMessagesToSend(now)

    messages should have size 1

    val newMessages = scheduler.retrieveMessagesToSend(now.plusSeconds(10))

    newMessages should have size 0
  }

  test("should schedule for different chats separately") {
    val scheduler = new CommandsScheduler()

    val scheduleTime = now.minusMinutes(6)
    scheduler.scheduleCommand(testCommand, testCron, testChatId, scheduleTime)

    val anotherChatId = 111
    val anotherScheduleTime = now.minusMinutes(2)
    scheduler.scheduleCommand(testCommand, testCron, anotherChatId, anotherScheduleTime)

    val messages = scheduler.retrieveMessagesToSend(now)

    messages should have size 1

    val message = messages.head
    message.from should be (BOT_USER)
    message.chat.id should be (testChatId)
  }

  test("should return no commands if time is not come") {
    val scheduler = new CommandsScheduler()

    val scheduleTime = now.minusMinutes(4)
    scheduler.scheduleCommand(testCommand, testCron, testChatId, scheduleTime)

    val messages = scheduler.retrieveMessagesToSend(now)

    messages should have size 0
  }

  test("should remove schedule by chat id and command") {
    val scheduler = new CommandsScheduler()

    val scheduleTime = now.minusMinutes(6)
    scheduler.scheduleCommand(testCommand, testCron, testChatId, scheduleTime)

    val anotherChatId = 111
    val anotherScheduleTime = now.minusMinutes(6)
    scheduler.scheduleCommand(testCommand, testCron, anotherChatId, anotherScheduleTime)

    scheduler.removeCommandSchedule(testCommand, testChatId)

    val messages = scheduler.retrieveMessagesToSend(now)

    messages should have size 1

    val message = messages.head
    message.from should be (BOT_USER)
    message.chat.id should be (anotherChatId)
  }
}
