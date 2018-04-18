package com.kgribov.telegram

import scala.concurrent.duration._

import com.kgribov.telegram.process.DialogAnswers

package object dsl {

  val submitSuccessMes = (_: String) => "Answer submitted"

  def askSelectQuestionOnAnswers(question: DialogAnswers => SelectQuestion,
                                 submitAlert: String => String = submitSuccessMes,
                                 alreadyAnsweredAlert: String = "You have already answered to this question",
                                 questionTTL: Option[Duration] = None): AskSelectQuestion = {
    AskSelectQuestion(question, submitAlert, alreadyAnsweredAlert, questionTTL)
  }

  def askSelectQuestion(question: String,
                        possibleAnswers: Seq[String],
                        submitAlert: String => String = submitSuccessMes,
                        alreadyAnsweredAlert: String = "You have already answered to this question",
                        questionTTL: Option[Duration] = None): AskSelectQuestion = {
    AskSelectQuestion((_: DialogAnswers) => SelectQuestion(question, possibleAnswers), submitAlert, alreadyAnsweredAlert, questionTTL)
  }

  def askQuestion(questionText: String): AskTextQuestion = {
    AskTextQuestion(_ => questionText)
  }

  def askQuestionOnAnswers(question: DialogAnswers => String): AskQuestion = {
    AskTextQuestion(question)
  }

  def askQuiz(question: String,
              selectAnswer: QuizAllAnswers,
              alertsMessages: AlertsMessages = FixedAlertsMessages(),
              collectAnswers: AnswersCollector = AnswersCollector(),
              quizTTL: Duration = 0.seconds): AskQuiz = {

    new AskQuiz(
      question,
      selectAnswer,
      alertsMessages,
      collectAnswers,
      quizTTL
    )
  }

  def submitAnswers(replyOnSubmit: String, collectAnswers: DialogAnswers => Unit): CollectAnswers = {
    new CollectAnswers(answers => {
      collectAnswers(answers)
      replyOnSubmit
    })
  }

  def submitAnswers(replyOnSubmit: DialogAnswers => String): CollectAnswers = {
    new CollectAnswers(replyOnSubmit)
  }
}
