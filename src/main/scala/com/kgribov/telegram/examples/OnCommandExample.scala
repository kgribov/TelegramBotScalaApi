package com.kgribov.telegram.examples

import java.util.concurrent.atomic.AtomicInteger

import com.kgribov.telegram.dsl.BotSchema

import scala.util.{Random, Try}

object OnCommandExample extends App {

  val apiKey = ""

  def botSchema(apiKey: String): BotSchema = {
    val counter = new AtomicInteger()

    new BotSchema(apiKey, "OnCommandExample")

      .replyOnCommand("random", _ => s"Random number: ${Random.nextInt(100)}")

      .onCommand("add", message => {
        val inputNumber = Try(message.text.toInt).getOrElse(0)
        counter.addAndGet(inputNumber)
      })

      .replyOnCommand("result", _ => s"Your result is ${counter.get()}")
  }

  botSchema(apiKey).startBot()
}
