package com.kgribov.telegram.examples

import scala.concurrent.duration._
import com.kgribov.telegram.bot.schema._

object QuizExample extends App {

  val apiKey = ""

  def botSchema(): BotSchema = {
    botSchema()
      .startDialogOnCommand("start_quiz", DialogSchema(
        questions = Seq(
        ),
        personalDialog = false
      ))
  }

  createBotSchema()
    .startBot(apiKey)
}
