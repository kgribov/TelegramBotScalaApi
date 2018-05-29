package com.kgribov.telegram.bot.dialog

import com.kgribov.telegram.bot.func.UserStateAction

import scala.concurrent.duration._

case class Dialog(multiUserEnabled: Boolean = false,
                  dialogTTL: Duration = 1.hour,
                  questions: Seq[DialogQuestion] = Seq.empty,
                  submitAnswers: Map[String, Seq[Answer]] => String,
                  actions: Map[String, Seq[Answer]] => Seq[UserStateAction] = _ => Seq.empty)
