package com.kgribov.telegram.examples

import com.kgribov.telegram.dsl._
import com.kgribov.telegram.security._

object SimpleDialogWithKeyboard extends App {

  val apiKey = ""

  val askPersonalInfo = Dialog(
    questions = Seq(
      askQuiz(Quiz("What is your gender", Seq("Male", "Female"))),
      askQuiz(Quiz("What is your age", Seq("Under 30", "30 and more"))),
      submitAnswers(answers => {
        val allAnswers = answers.allTextAnswers
        s"Thanks for ask, your answers are: [${allAnswers.values.toList}]"
      })
    ), withUserOnly = false
  )

  val botSchema = new BotSchema(apiKey, "simpleAsk")
    .startDialogOnCommand("ask", askPersonalInfo, withPermissions = allowEverything())
    .replyOnCommand("ask2", _ => "Some shit")

  botSchema.startBot()
}
