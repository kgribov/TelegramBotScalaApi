package com.kgribov.telegram

import com.kgribov.telegram.process.DialogAnswers

import scala.concurrent.duration.Duration

package object dsl {

  val submitSuccessMes = (_: String) => "Answer submitted"

  def askQuizOnAnswer(quiz: DialogAnswers => Quiz,
                      submitAlert: String => String = submitSuccessMes,
                      questionTTL: Option[Duration] = None): AskQuizQuestion = {
    AskQuizQuestion(quiz, submitAlert, questionTTL)
  }

  def askQuiz(quiz: Quiz,
              submitAlert: String => String = submitSuccessMes,
              questionTTL: Option[Duration] = None): AskQuizQuestion = {
    AskQuizQuestion((_: DialogAnswers) => quiz, submitAlert, questionTTL)
  }

  def askQuestion(questionText: String): AskTextQuestion = {
    AskTextQuestion(_ => questionText)
  }

  def askQuestion(question: DialogAnswers => String): AskQuestion = {
    AskTextQuestion(question)
  }

  def submitAnswers(replyOnSubmit: String, collectAnswers: DialogAnswers => Unit): CollectAnswers = {
    CollectAnswers(answers => {
      collectAnswers(answers)
      replyOnSubmit
    })
  }

  def submitAnswers(replyOnSubmit: DialogAnswers => String): CollectAnswers = {
    CollectAnswers(replyOnSubmit)
  }
}
