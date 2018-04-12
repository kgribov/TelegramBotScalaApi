package com.kgribov.telegram.examples

import scala.concurrent.duration._
import com.kgribov.telegram.dsl._

object QuizExample extends App {

  val apiKey = ""

  def botSchema(apiKey: String): BotSchema = {
    new BotSchema(apiKey, "QuizExample")
      .startDialogOnCommand("start_quiz", Dialog(
        questions = Seq(
          askQuiz(
            question = "Who was the first president of America?",
            selectAnswer = QuizAnswers("Bush", "Lincoln", "Jefferson")("Washington")
          ),
          askQuiz(
            question = "What is a capital of Russia?",
            selectAnswer = QuizAnswers("Saint-Petersburg", "Smolensk")("Moscow"),
            alertsMessages = FixedAlertsMessages(
              wrongAnswerAlert = "Seems like you have never been in Russia =)"
            ),
            collectAnswers = AnswersCollector(
              collectWrongAnswer = (message) => println(s"User ${message.from} have never been in Russia")
            ),
            quizTTL = 1.minute
          )
        ),
        personalDialog = false
      ))
  }

  botSchema(apiKey)
    .startBot()
}
