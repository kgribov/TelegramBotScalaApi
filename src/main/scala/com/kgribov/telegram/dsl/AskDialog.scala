package com.kgribov.telegram.dsl

import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

import com.kgribov.telegram.process._
import com.kgribov.telegram.sender.MessageSender

import scala.concurrent.duration.Duration

case class AskDialog(withUserOnly: Boolean = false,
                     dialogTTL: Duration = Duration(5, TimeUnit.MINUTES),
                     questions: Seq[AskQuestion])

trait AskQuestion {
  def toQuestion(messageSender: MessageSender): DialogAnswers => DialogQuestion
}

case class AskTextQuestion(textToAsk: DialogAnswers => String) extends AskQuestion {

  override def toQuestion(messageSender: MessageSender): (DialogAnswers) => DialogQuestion =
    (currentAnswers: DialogAnswers) =>
      TextDialogQuestion(messageSender, textToAsk(currentAnswers))
}

case class AskQuizQuestion(quiz: DialogAnswers => Quiz,
                           submitAlert: String => String,
                           questionTTL: Option[Duration] = None) extends AskQuestion {

  private def expiredAt: Option[ZonedDateTime] = questionTTL.map(duration => ZonedDateTime.now().plusNanos(duration.toNanos))

  override def toQuestion(messageSender: MessageSender): (DialogAnswers) => DialogQuestion =
    (currentAnswers: DialogAnswers) => {
      val quizToAsk = quiz(currentAnswers)
      KeyboardDialogQuestion(messageSender, quizToAsk.question, quizToAsk.possibleAnswers.toList, submitAlert, expiredAt)
    }
}

case class Quiz(question: String, possibleAnswers: Seq[String])

case class CollectAnswers(submitText: DialogAnswers => String) extends AskQuestion {
  override def toQuestion(messageSender: MessageSender): (DialogAnswers) => DialogQuestion =
    (currentAnswers: DialogAnswers) => {
      DummyDialogQuestion(submitText(currentAnswers), messageSender)
    }
}
