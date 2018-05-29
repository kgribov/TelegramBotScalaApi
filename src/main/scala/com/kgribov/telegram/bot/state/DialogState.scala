package com.kgribov.telegram.bot.state

import java.time.LocalDateTime

import com.kgribov.telegram.bot.dialog.{Answer, Dialog, DialogQuestion}
import com.kgribov.telegram.bot.model.Chat

case class DialogState(dialogId: String,
                       chat: Chat,
                       dialog: Dialog,
                       notDoneQuestions: Seq[QuestionState],
                       doneQuestions: Seq[QuestionState] = Seq.empty,
                       expiredAt: LocalDateTime) {
}

case class QuestionState(question: DialogQuestion,
                         askedAt: Option[LocalDateTime] = None,
                         withUserOnlyId: Option[Long] = None,
                         answers: Seq[Answer] = Seq.empty)
