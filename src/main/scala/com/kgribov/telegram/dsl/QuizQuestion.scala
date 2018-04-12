package com.kgribov.telegram.dsl

import java.time.ZonedDateTime

import com.kgribov.telegram.model.Message
import com.kgribov.telegram.process.{DialogAnswers, DialogQuestion, KeyboardDialogQuestion}
import com.kgribov.telegram.sender.MessageSender

import scala.concurrent.duration.Duration
import scala.util.Random

class AskQuiz(question: String,
              selectAnswer: QuizAllAnswers,
              alertsMessages: AlertsMessages,
              collectAnswers: AnswersCollector,
              quizTTL: Duration) extends AskQuestion {

  private def expiredAt: Option[ZonedDateTime] = {
    if (quizTTL.toNanos == 0) {
      None
    } else {
      Some(ZonedDateTime.now().plusNanos(quizTTL.toNanos))
    }
  }

  override def toQuestion(messageSender: MessageSender): (DialogAnswers) => DialogQuestion = {
    def submitAlert(answer: Message): String = {
      if (selectAnswer.isRightAnswer(answer.text)) {
        collectAnswers.collectRightAnswer(answer)
        alertsMessages.rightAnswerAlert
      } else {
        collectAnswers.collectWrongAnswer(answer)
        alertsMessages.wrongAnswerAlert
      }
    }
    (_: DialogAnswers) =>
      KeyboardDialogQuestion(messageSender, question, selectAnswer.allAnswers, submitAlert, alertsMessages.alreadyAnsweredAlert, expiredAt)
  }
}

case class AnswersCollector(collectRightAnswer: Message => Unit = _ => {},
                            collectWrongAnswer: Message => Unit = _ => {})

case class QuizAnswers(wrongAnswers: String*) extends ((String) => QuizAllAnswers) {
  override def apply(rightAnswer: String) = QuizAllAnswers(wrongAnswers, rightAnswer)
}

case class QuizAllAnswers(wrongAnswers: Seq[String], rightAnswer: String) {

  def isRightAnswer(answer: String): Boolean = answer == rightAnswer

  def allAnswers: List[String] = Random.shuffle(rightAnswer :: wrongAnswers.toList)
}

trait AlertsMessages {
  def rightAnswerAlert: String

  def wrongAnswerAlert: String

  def alreadyAnsweredAlert: String
}

case class FixedAlertsMessages(rightAnswerAlert: String = "Right answer!",
                               wrongAnswerAlert: String = "Sorry, it is wrong answer",
                               alreadyAnsweredAlert: String = "You have already answered to this quiz") extends AlertsMessages

case class RandomAlertsMessages(rightAnswerAlerts: Seq[String] = Seq("Right answer!", "You are right"),
                             wrongAnswerAlerts: Seq[String] = Seq("Sorry, it is wrong answer", "Wrong answer"),
                             alreadyAnsweredAlerts: Seq[String] = Seq("You have already answered to this quiz")) extends AlertsMessages {

  def rightAnswerAlert: String = {
    randomFromSeq(rightAnswerAlerts)
  }

  def wrongAnswerAlert: String = {
    randomFromSeq(wrongAnswerAlerts)
  }

  def alreadyAnsweredAlert: String = {
    randomFromSeq(alreadyAnsweredAlerts)
  }

  private def randomFromSeq(seq: Seq[String]): String = {
    seq(Random.nextInt(seq.size))
  }
}
