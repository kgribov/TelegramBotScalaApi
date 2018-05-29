package com.kgribov.telegram.bot.schema

import com.kgribov.telegram.bot.dialog._

import scala.concurrent.duration.Duration

trait QuestionSchema {
  def toDialogQuestion: DialogQuestion
}

case class TextQuestionSchema(question: String,
                              questionTTL: Option[Duration] = None,
                              rejectAnswer: String => Option[String] = _ => None,
                              withOnlyOneUser: Boolean = true) extends QuestionSchema {

  override def toDialogQuestion = new TextQuestion(
    question = question,
    questionTTL = questionTTL,
    rejectAnswer = rejectAnswer,
    withOnlyOneUser = withOnlyOneUser
  )
}

case class SelectQuestionSchema(question: String,
                                possibleAnswers: Seq[String],
                                alreadyAnsweredNotification: String,
                                sendNotification: Answer => Option[String],
                                questionTTL: Option[Duration] = None,
                                withOnlyOneUser: Boolean = true) extends QuestionSchema {

  override def toDialogQuestion = new SelectQuestion(
    question: String,
    possibleAnswers: Seq[String],
    sendNotification: Answer => Option[String],
    questionTTL = questionTTL,
    withOnlyOneUser = withOnlyOneUser,
    alreadyAnsweredNotification = alreadyAnsweredNotification
  )
}

case class NotificationQuestionSchema(notificationText: String) extends QuestionSchema {

  override def toDialogQuestion = new NotificationQuestion(notificationText)
}
