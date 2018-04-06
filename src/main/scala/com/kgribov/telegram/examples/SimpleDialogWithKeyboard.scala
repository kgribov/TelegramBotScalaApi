package com.kgribov.telegram.examples

import com.kgribov.telegram.dsl._
import com.kgribov.telegram.security._

object SimpleDialogWithKeyboard extends App {

  val apiKey = ""

  val askPersonalInfo = Dialog(
    questions = Seq(
      askSelectQuestion("What is your gender", Seq("Male", "Female")),
      askSelectQuestion("What is your age", Seq("Under 30", "30 and more")),
      submitAnswers(answers => {
        val allAnswers = answers.allTextAnswers
        s"Thanks for ask, your answers are: [${allAnswers.values.toList}]"
      })
    )
  )

  val botSchema = new BotSchema(apiKey, "simpleAsk")
    .replyOnCommand("id", message => s"userId: ${message.from.id} chatId: ${message.chat.id}")

  botSchema.startBot()
}
