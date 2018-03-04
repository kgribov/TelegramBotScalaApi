package com.kgribov.telegram.examples

import com.kgribov.telegram.dsl._

object SimpleDialogWithKeyboard extends App {

  val apiKey = ""

  val askPersonalInfo = AskDialog(
    withUserOnly = true,
    questions = Seq(
      askQuiz(Quiz("What is your gender", Seq("Male", "Female"))),
      askQuiz(Quiz("What is your age", Seq("Under 30", "30 and more"))),
      submitAnswers(answers => {
        val allAnswers = answers.allTextAnswers
        s"Thanks for ask, your answers are: [${allAnswers.values.toList}]"
      })
    )
  )

  val botSchema = new BotSchema(apiKey)
    .startDialogOnCommand("ask", askPersonalInfo)

  botSchema.startBot()
}
