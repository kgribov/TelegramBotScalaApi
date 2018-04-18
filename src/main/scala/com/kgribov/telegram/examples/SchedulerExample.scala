package com.kgribov.telegram.examples

import com.kgribov.telegram.dsl.BotSchema
import com.kgribov.telegram.security._
import com.kgribov.telegram.scheduler.CommandsScheduler

object SchedulerExample extends App {
  val apiKey = ""

  val scheduler = new CommandsScheduler(Some("scheduler_store"))

  def botSchema(apiKey: String): BotSchema = {
    new BotSchema(apiKey, "scheduler")
      .replyOnCommand("subscribe", message => {
        val everyMinute = "0 * * ? * *"
        scheduler.scheduleCommand("new_post", everyMinute, message.chat.id)
        "You have subscribed on new posts!"
      })
      .replyOnCommand("new_post", _ => "It is a new post every minute!", withPermissions = allowBotOnly())
  }

  botSchema(apiKey)
    .startBot(internMessagesSources = Seq(scheduler.retrieveMessagesToSend))
}
