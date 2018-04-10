package com.kgribov.telegram.examples

import com.kgribov.telegram.dsl._
import com.kgribov.telegram.security._
import scala.concurrent.duration._

object DialogExample extends App {

  val apiKey = ""

  def botSchema(apiKey: String): BotSchema = {
    val askPersonalInfo = Dialog(
      questions = Seq(
        askSelectQuestion(
          "What is your gender",
          Seq("Male", "Female"),
          submitAlert = _ => "I like your answer!"
        ),

        askSelectQuestion(
          "What is your age (question is actual for 5 seconds)",
          Seq("Under 30", "30 and more"),
          questionTTL = Some(5.seconds)
        ),

        askQuestion("What is your name?"),

        submitAnswers(answers => {
          val allAnswers = answers.allTextAnswers
          s"Thanks for ask, your answers are: [${allAnswers.values.mkString(",")}]"
        })
      ),
      personalDialog = true,
      dialogTTL = 1.minute
    )

    new BotSchema(apiKey, "simpleAsk")
      .startDialogOnCommand("ask", askPersonalInfo, withPermissions = allowPrivateChats())
  }

  botSchema(apiKey).startBot()
}