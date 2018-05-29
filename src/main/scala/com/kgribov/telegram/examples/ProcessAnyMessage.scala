package com.kgribov.telegram.examples

import java.util.concurrent.atomic.AtomicInteger

import com.kgribov.telegram.bot.schema._

object ProcessAnyMessage extends App {

  val apiKey = ""

  def botSchema(): BotSchema = {
    val messagesCounter = new AtomicInteger()

    createBotSchema()
      .onMessage(_ => messagesCounter.addAndGet(1))
      .replyOnMessage(
        filter = message => message.text.equals("count"),
        _ => s"Your number of messages is ${messagesCounter.get()}"
      )
  }

  botSchema().startBot(apiKey)
}
