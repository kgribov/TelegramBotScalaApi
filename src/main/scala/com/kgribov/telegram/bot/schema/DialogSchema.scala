package com.kgribov.telegram.bot.schema

import java.util.concurrent.TimeUnit

import com.kgribov.telegram.bot.dialog.{Answer, Dialog}
import com.kgribov.telegram.bot.func.{UserBotFunction, UserStateAction}
import com.kgribov.telegram.bot.func.user.OnCommandCreateDialogFunc

import scala.concurrent.duration.Duration

case class DialogSchema(personalDialog: Boolean = true,
                        dialogTTL: Duration = Duration(5, TimeUnit.MINUTES),
                        questions: Seq[QuestionSchema] = Seq.empty,
                        submitAnswers: Map[String, Seq[Answer]] => String = _ => "Thanks for your answers!",
                        actions: Map[String, Seq[Answer]] => Seq[UserStateAction] = _ => Seq.empty) {

  def toBotFunc(command: String): UserBotFunction = {
    val dialog = Dialog(
      multiUserEnabled = !personalDialog,
      dialogTTL = dialogTTL,
      questions = questions.map(_.toDialogQuestion),
      submitAnswers = submitAnswers,
      actions = actions
    )

    new OnCommandCreateDialogFunc(command, dialog)
  }
}
