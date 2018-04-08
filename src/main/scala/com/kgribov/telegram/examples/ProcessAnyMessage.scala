package com.kgribov.telegram.examples

import java.util.concurrent.atomic.AtomicInteger

import com.kgribov.telegram.dsl.BotSchema

object ProcessAnyMessage extends App {

  val apiKey = ""

  def botSchema(apiKey: String): BotSchema = {
    val messagesCounter = new AtomicInteger()

    new BotSchema(apiKey, "ProcessAnyMessage")

      .onMessage(_ => messagesCounter.addAndGet(1))

      .replyOnMessage(
        filter = message => message.text.equals("count"),
        _ => s"Your number of messages is ${messagesCounter.get()}"
      )
  }

  botSchema(apiKey).startBot()
}
