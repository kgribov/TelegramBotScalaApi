package com.kgribov.telegram.examples

import com.kgribov.telegram.bot.schema._

object SchedulerExample extends App {
  val apiKey = ""

  def botSchema(): BotSchema = {
    val command = "news"
    val cron = "0 * * ? * *"

    val unsubscribe = DialogSchema(
      submitAnswers = _ => "Your subscription was deleted",
      actions = _ => Seq(
        removeSchedule(command, cron)
      )
    )

    val subscribeOnNews = DialogSchema(
      submitAnswers = _ => "Thanks for your subscription",
      actions = _ => Seq(
        createSchedule(command, cron)
      )
    )

    createBotSchema()
      .replyOnCommand("news", _=> "It's a new post of news!")

      .startDialogOnCommand("subscribe", subscribeOnNews, withPermissions = allowPrivateChats())

      .startDialogOnCommand("unsubscribe", unsubscribe, withPermissions = allowPrivateChats())
  }

  botSchema().startBot(apiKey)
}
