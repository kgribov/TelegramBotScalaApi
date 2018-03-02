package com.kgribov.telegram.process

import com.kgribov.telegram.model.User

class DialogAnswers(answers: List[(String, Answer)] = List()) {

  def withAnswer(question: String, answer: Answer): DialogAnswers = {
    new DialogAnswers((question, answer) :: answers)
  }

  def lastAnswer: Answer = {
    if (answers.isEmpty) {
      throw new Exception("You should ask something before read answers")
    }
    answers.head._2
  }

  def lastTextAnswer: String = {
    if (answers.isEmpty) {
      throw new Exception("You should ask something before read answers")
    }
    answers.head._2.simpleAnswer.text
  }

  def allTextAnswers: Map[String, String] = answers.groupBy(_._1).map(answers =>
    (answers._1, answers._2.head._2.simpleAnswer.text)
  )

  def lastAnswersFromUsers: List[(User, String)] =
    answers
      .head
      ._2
      .answers
      .groupBy(_.from)
      .map(userAnswers => (userAnswers._1, userAnswers._2.head.text))
      .toList
}
