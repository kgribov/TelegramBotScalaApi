package com.kgribov.telegram.examples

import com.kgribov.telegram.bot.schema._

import scala.util.Random

object OnCommandExample extends App {

  val apiKey = ""

  def botSchema(): BotSchema = {
    createBotSchema()
      .replyOnCommand("random", _ => s"Random number: ${Random.nextInt(100)}")
  }

  botSchema().startBot(apiKey)
}
