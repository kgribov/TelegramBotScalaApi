package com.kgribov.telegram.process

import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

import com.kgribov.telegram.model.{Message, User}
import com.kgribov.telegram.sender.MessageSender

import scala.concurrent.duration.Duration

class DialogProcessor(chatId: Int,
                      dialogTimeout: Duration = Duration(5, TimeUnit.MINUTES),
                      messageSender: MessageSender,
                      questions: Iterable[DialogAnswers => DialogQuestion],
                      withUserOnly: Option[User],
                      now: ZonedDateTime = ZonedDateTime.now()) {

  private val expiredTime = now.plusNanos(dialogTimeout.toNanos)

  private var questionsToAsk = questions
  private var currentQuestion: Option[DialogQuestion] = None
  private var answers = new DialogAnswers

  def processMessages(messages: Option[List[Message]]): Unit = {
    if (isActive()) {
      if (questionIsNotAskedYet) {
        askQuestion()
      } else {
        val orderedMes = messages.map(_.sortBy(_.date.toInstant.toEpochMilli))
        orderedMes.foreach(_.foreach(process))
      }

      if (currentQuestion.exists(_.isDone().isDefined)) {
        val question = currentQuestion.get
        answers = answers.withAnswer(question.questionText, question.isDone().get)
        moveToNextQuestion()
      }
    }
  }

  private def process(message: Message): Unit = {
    currentQuestion.foreach(question => {
      if (questionFromUser(message)) {
        question.processReply(message)
      }
    })
  }

  def isActive(currentTime: ZonedDateTime = ZonedDateTime.now()): Boolean = {
    expiredTime.isAfter(currentTime) && dialogIsNotEnd
  }

  private def questionIsNotAskedYet: Boolean = currentQuestion.isEmpty

  private def moveToNextQuestion(): Unit = {
    currentQuestion = None
    questionsToAsk = questionsToAsk.tail
  }

  private def questionFromUser(message: Message): Boolean = {
    if (withUserOnly.isEmpty) {
      true
    } else {
      withUserOnly.get == message.from
    }
  }

  private def askQuestion(): Unit = {
    currentQuestion = Some(questionsToAsk.head(answers))
    currentQuestion.get.askQuestion(chatId)
  }

  private def dialogIsNotEnd: Boolean = questionsToAsk.nonEmpty || currentQuestion.isDefined
}


