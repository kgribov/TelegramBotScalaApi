package com.kgribov.telegram.examples

import com.kgribov.telegram.bot.schema._

import scala.concurrent.duration._

object DialogExample extends App {

  val apiKey = ""

  def botSchema(): BotSchema = {
    val askPersonalInfo = DialogSchema(
      questions = Seq(
        askSelectQuestion(
          question = "What is your gender",
          Seq("Male", "Female"),
          submitAlert = _ => Some("I like your answer!")
        ),

        askSelectQuestion(
          "What is your age (question is actual for 5 seconds)",
          Seq("Under 30", "30 and more"),
          questionTTL = Some(5.seconds)
        ),

        askQuestion("What is your name?")
      ),

      dialogTTL = 1.minute,

      submitAnswers = answers => {
        val answersText = answers.map {
          case (questionText, usersAnswers) =>
            s"$questionText -> ${usersAnswers.map(_.answer).mkString(", ")}"
        }.mkString("\n")
        s"Your answers is: \n $answersText"
      }
    )

    createBotSchema()
      .startDialogOnCommand("ask", askPersonalInfo, withPermissions = allowPrivateChats())
  }

  botSchema().startBot(apiKey)
}
