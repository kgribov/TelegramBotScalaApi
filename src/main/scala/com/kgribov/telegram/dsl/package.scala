package com.kgribov.telegram

import com.kgribov.telegram.model.Message
import com.kgribov.telegram.process.DialogAnswers

import scala.concurrent.duration.Duration

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
              selectAnswer: Seq[String],
              quizAnswer: String,
              collectRightAnswers: Message => Unit = message => (),
              collectMistakes: Message => Unit = message => (),
              rightAnswerAlert: => String = "Yeah! You are right!",
              wrongAnswerAlert: => String = "Sorry, it is wrong answer",
              alreadyAnsweredAlert: => String = "You have already answered to this quiz",
              quizTTL: Option[Duration] = None): AskQuiz = {
    new AskQuiz(
      question,
      selectAnswer,
      quizAnswer,
      collectRightAnswers,
      collectMistakes,
      rightAnswerAlert,
      wrongAnswerAlert,
      alreadyAnsweredAlert,
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
