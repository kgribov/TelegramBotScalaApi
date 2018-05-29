package com.kgribov.telegram.bot

import com.kgribov.telegram.bot.dialog.Answer
import com.kgribov.telegram.bot.func.UserStateAction
import com.kgribov.telegram.bot.func.action.{CreateScheduleAction, RemoveScheduleAction}
import com.kgribov.telegram.bot.security._

import scala.concurrent.duration.Duration

package object schema {

  def createBotSchema(): BotSchema = {
    BotSchema()
  }

  def askSelectQuestion(question: String,
                        possibleAnswers: Seq[String],
                        withOnlyOneUser: Boolean = true,
                        submitAlert: Answer => Option[String] = _ => None,
                        alreadyAnsweredAlert: String = "You have already answered to this question",
                        questionTTL: Option[Duration] = None): QuestionSchema = {
    SelectQuestionSchema(
      question = question,
      possibleAnswers = possibleAnswers,
      questionTTL = questionTTL,
      withOnlyOneUser = withOnlyOneUser,
      sendNotification = submitAlert,
      alreadyAnsweredNotification = alreadyAnsweredAlert
    )
  }

  def askQuestion(questionText: String,
                  questionTTL: Option[Duration] = None,
                  rejectAnswer: String => Option[String] = _ => None,
                  withOnlyOneUser: Boolean = true): QuestionSchema = {
    TextQuestionSchema(
      question = questionText,
      questionTTL = questionTTL,
      rejectAnswer = rejectAnswer,
      withOnlyOneUser = withOnlyOneUser
    )
  }


  def askQuiz(): QuestionSchema = ???

  def createSchedule(commandToExecute: String, cronExpr: String): UserStateAction = {
    new CreateScheduleAction(commandToExecute, cronExpr)
  }

  def removeSchedule(commandToExecute: String, cronExpr: String): UserStateAction = {
    new RemoveScheduleAction(commandToExecute, cronExpr)
  }

  def allowBotOnly(): ChatPermission = {
    val botId = model.BOT_USER.id
    new MultiPermissions(Seq(allowPrivateChats(Seq(botId)), allowGroups(onlyForGroups = Seq(botId))))
  }

  def allowEverything(onlyForUsers: Seq[Long] = Seq(),
                      groupUserType: UserType = ANYONE,
                      onlyForGroups: Seq[Long] = Seq()): ChatPermission = {
    new MultiPermissions(Seq(allowPrivateChats(onlyForUsers), allowGroups(groupUserType, onlyForGroups)))
  }

  def allowGroups(groupUserType: UserType = ANYONE, onlyForGroups: Seq[Long] = Seq()): ChatPermission = {
    new GroupsAllowed(groupUserType, onlyForGroups)
  }

  def allowPrivateChats(onlyForUsers: Seq[Long] = Seq()): ChatPermission = {
    new PrivateChatAllowed(onlyForUsers)
  }
}
