package com.kgribov.telegram.examples

import com.kgribov.telegram.dsl.BotSchema

object QuizExample extends App {

  val apiKey = ""

  def botSchema(apiKey: String): BotSchema = {
    new BotSchema(apiKey, "QuizExample")
    // TODO
  }

  botSchema(apiKey).startBot()
}
