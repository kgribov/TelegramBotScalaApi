package com.kgribov.telegram.bot.func.user

import java.time.LocalDateTime

import com.kgribov.telegram.bot.dialog.Dialog
import com.kgribov.telegram.bot.func.UserBotFunction
import com.kgribov.telegram.bot.loader.TelUpdate
import com.kgribov.telegram.bot.sender.TelBotReply
import com.kgribov.telegram.bot.state.{BotState, DialogState, QuestionState}

class OnCommandCreateDialogFunc(command: String,
                                dialog: Dialog,
                                now: LocalDateTime = LocalDateTime.now()) extends UserBotFunction {

  override def apply(update: TelUpdate): (BotState, TelBotReply) = {
    val userAskCommandInChat = update
      .commandMessages
      .filter(_.command == command)
      .map(message => (message.id, message.chat, message.from.id))

    val dialogsStates = userAskCommandInChat.map{ case (messageId, chat, userId) =>
      DialogState(
        dialogId = messageId,
        chat = chat,
        dialog = dialog,
        notDoneQuestions = dialog.questions.map(
          question => QuestionState(question = question, withUserOnlyId = withUserOnly(question.askOnlyOneUser, userId))
        ),
        expiredAt = now.plusSeconds(dialog.dialogTTL.toSeconds),
      )
    }

    (BotState(dialogsStates), TelBotReply())
  }

  private def withUserOnly(askOnlyOneUser: Boolean, userId: Long): Option[Long] = {
    if (askOnlyOneUser) {
      Some(userId)
    } else {
      None
    }
  }
}
