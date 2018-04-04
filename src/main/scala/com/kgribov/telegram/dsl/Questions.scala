package com.kgribov.telegram.dsl

import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

import com.kgribov.telegram.model.Message
import com.kgribov.telegram.process._
import com.kgribov.telegram.sender.MessageSender

import scala.concurrent.duration.Duration

case class Dialog(personalDialog: Boolean = true,
                  dialogTTL: Duration = Duration(5, TimeUnit.MINUTES),
                  questions: Iterable[AskQuestion])

trait AskQuestion {
  def toQuestion(messageSender: MessageSender): DialogAnswers => DialogQuestion
}

case class AskTextQuestion(textToAsk: DialogAnswers => String) extends AskQuestion {

  override def toQuestion(messageSender: MessageSender): (DialogAnswers) => DialogQuestion =
    (currentAnswers: DialogAnswers) =>
      TextDialogQuestion(messageSender, textToAsk(currentAnswers))
}

case class AskSelectQuestion(question: DialogAnswers => SelectQuestion,
                             submitAlert: String => String,
                             alreadyAnsweredAlert: String,
                             questionTTL: Option[Duration] = None) extends AskQuestion {

  private def expiredAt: Option[ZonedDateTime] = questionTTL.map(duration => ZonedDateTime.now().plusNanos(duration.toNanos))

  override def toQuestion(messageSender: MessageSender): (DialogAnswers) => DialogQuestion =
    (currentAnswers: DialogAnswers) => {
      val questionToAsk = question(currentAnswers)
      def submitMessage(message: Message): String = submitAlert(message.text)
      KeyboardDialogQuestion(
        messageSender,
        questionToAsk.question,
        questionToAsk.possibleAnswers.toList,
        submitMessage,
        alreadyAnsweredAlert,
        expiredAt
      )
    }
}

case class SelectQuestion(question: String, possibleAnswers: Seq[String])

class CollectAnswers(submitText: DialogAnswers => String) extends AskQuestion {
  override def toQuestion(messageSender: MessageSender): (DialogAnswers) => DialogQuestion =
    (currentAnswers: DialogAnswers) => {
      DummyDialogQuestion(submitText(currentAnswers), messageSender)
    }
}

class AskQuiz(question: String,
                   selectAnswer: Seq[String],
                   quizAnswer: String,
                   collectRightAnswers: Message => Unit,
                   collectMistakes: Message => Unit,
                   rightAnswerAlert: => String,
                   wrongAnswerAlert: => String,
                   alreadyAnsweredAlert: => String,
                   quizTTL: Option[Duration] = None) extends AskQuestion {

  private def expiredAt: Option[ZonedDateTime] = quizTTL.map(duration => ZonedDateTime.now().plusNanos(duration.toNanos))

  override def toQuestion(messageSender: MessageSender): (DialogAnswers) => DialogQuestion = {
    def submitAlert(answer: Message): String = {
      if (answer.text == quizAnswer) {
        collectRightAnswers(answer)
        rightAnswerAlert
      } else {
        collectMistakes(answer)
        wrongAnswerAlert
      }
    }
    (_: DialogAnswers) =>
      KeyboardDialogQuestion(messageSender, question, selectAnswer.toList, submitAlert, alreadyAnsweredAlert, expiredAt)
  }
}
