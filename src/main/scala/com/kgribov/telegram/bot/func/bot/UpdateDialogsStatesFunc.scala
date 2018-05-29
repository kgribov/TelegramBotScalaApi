package com.kgribov.telegram.bot.func.bot

import java.time.LocalDateTime

import com.kgribov.telegram.bot.dialog.Answer
import com.kgribov.telegram.bot.func.{UpdateBotFunction, UserStateAction}
import com.kgribov.telegram.bot.loader.TelUpdate
import com.kgribov.telegram.bot.model.{BotTextReply, Chat}
import com.kgribov.telegram.bot.sender.TelBotReply
import com.kgribov.telegram.bot.state.{BotState, DialogState, QuestionState}

class UpdateDialogsStatesFunc extends UpdateBotFunction {

  override def apply(state: BotState, update: TelUpdate): (BotState, TelBotReply) = {
    val dialogsStates = state.dialogsStates

    val (updatedStates, rejectedReplies) = dialogsStates
      .map(state => updateDialogState(state, update))
      .unzip

    val statesWithClosedQuestions = updatedStates.map(dialogState => {
      if (needToCloseCurrentQuestion(dialogState)) {
        dialogWithDoneQuestion(dialogState)
      } else {
        dialogState
      }
    })

    val (askedStates, askQuestionReplies) = statesWithClosedQuestions
      .map(askQuestion)
      .unzip

    val submitDialogsReplies = askedStates
      .filter(!isNotDoneDialog(_))
      .map(repliesFromDoneDialog)

    val stateAfterActions = askedStates
      .filter(!isNotDoneDialog(_))
      .flatMap(dialogActions)
      .foldLeft(state)((state, chatAction) => chatAction._2(chatAction._1, state))


    val notDoneDialogs = askedStates.filter(isNotDoneDialog)

    (stateAfterActions.copy(dialogsStates = notDoneDialogs),
      mergeReplies(askQuestionReplies ++ rejectedReplies ++ submitDialogsReplies)
    )
  }

  private def dialogActions(dialogState: DialogState): Seq[(Chat, UserStateAction)] = {
    dialogState
      .dialog
      .actions(dialogAnswers(dialogState))
      .map((dialogState.chat, _))
  }

  private def dialogAnswers(dialogState: DialogState): Map[String, Seq[Answer]] = {
    dialogState
      .doneQuestions
      .map(questionState => (questionState.question.questionText, questionState.answers))
      .toMap
  }

  private def repliesFromDoneDialog(dialogState: DialogState): TelBotReply = {
    TelBotReply(
      textMessages = Seq(BotTextReply(
        dialogState.chat.id,
        dialogState.dialog.submitAnswers(dialogAnswers(dialogState)))
      )
    )
  }

  private def isNotDoneDialog(dialogState: DialogState): Boolean = {
    dialogState.expiredAt.isAfter(LocalDateTime.now()) && dialogState.notDoneQuestions.nonEmpty
  }

  private def mergeReplies(replies: Seq[TelBotReply]): TelBotReply = {
    replies.fold(TelBotReply())((reply1, reply2) => reply1.merge(reply2))
  }

  private def needToCloseCurrentQuestion(dialogState: DialogState): Boolean = {
    dialogState
      .notDoneQuestions
      .headOption.exists(questionIsDone)
  }

  private def questionIsDone(questionState: QuestionState): Boolean = {
    val question = questionState.question
    questionState.askedAt.exists(askedTime => question.isDone(questionState.answers, askedTime))
  }

  private def dialogWithDoneQuestion(dialogState: DialogState): DialogState = {
    val currentQuestionState = dialogState.notDoneQuestions.head
    dialogState.copy(
      notDoneQuestions = dialogState.notDoneQuestions.tail,
      doneQuestions = currentQuestionState +: dialogState.doneQuestions
    )
  }

  private def updateDialogState(dialogState: DialogState, update: TelUpdate): (DialogState, TelBotReply) = {
    val currentQuestionState = dialogState.notDoneQuestions.headOption

    currentQuestionState
      .map(questionState => updateQuestionState(questionState, dialogState.chat.id, update))
      .map{ case (questionState, rejectedReplies) =>
        (updateQuestionStateInDialogState(dialogState, questionState), rejectedReplies)
      }
      .getOrElse((dialogState, TelBotReply()))
  }

  private def updateQuestionStateInDialogState(dialogState: DialogState,
                                               questionState: QuestionState) = {
    dialogState.copy(notDoneQuestions = questionState +: dialogState.notDoneQuestions.tail)
  }

  private def updateQuestionState(questionState: QuestionState,
                                  chatId: String,
                                  update: TelUpdate): (QuestionState, TelBotReply) = {

    val question = questionState.question
    val messagesInChat = update.updateInChat(chatId)

    val messagesFromUser = questionState
      .withUserOnlyId
      .map(userId => messagesInChat.updateFromUser(userId))
      .getOrElse(messagesInChat)

    val allAnswers = question
      .getAnswers(messagesFromUser)

    val notifyOnAnswer = allAnswers.flatMap(answer => question.notifyOnAnswer(answer))
    val notRejectedAnswers = allAnswers.filter(
      answer => question
        .rejectAnswer(answer, alreadyAnswered(answer, questionState))
        .isEmpty
    )
    val rejectedReplies = allAnswers.flatMap(
      answer => question.rejectAnswer(
        answer,
        alreadyAnswered(answer, questionState)
      )
    )

    val updatedAnswers = questionState.answers ++ notRejectedAnswers

    (questionState.copy(answers = updatedAnswers), mergeReplies(rejectedReplies ++ notifyOnAnswer))
  }

  private def alreadyAnswered(answer: Answer, questionState: QuestionState): Boolean = {
    questionState.answers.exists(existAnswer => existAnswer.fromUser == answer.fromUser)
  }

  private def askQuestion(dialogState: DialogState): (DialogState, TelBotReply) = {
    val currentQuestionState = dialogState.notDoneQuestions.headOption

    currentQuestionState
      .filter(_.askedAt.isEmpty)
      .map(questionState => updateQuestionStateWithAsk(questionState, dialogState.chat.id))
      .map{ case (updatedQuestionState, replies) => (updateDialogStateWithAsk(dialogState, updatedQuestionState), replies)}
      .getOrElse((dialogState, TelBotReply()))
  }

  private def updateQuestionStateWithAsk(questionState: QuestionState, chatId: String): (QuestionState, TelBotReply) = {
    val reply = questionState.question.askQuestion(chatId)

    (questionState.copy(askedAt = Some(LocalDateTime.now())), reply)
  }

  private def updateDialogStateWithAsk(dialogState: DialogState, questionState: QuestionState): DialogState = {
    dialogState.copy(notDoneQuestions = questionState +: dialogState.notDoneQuestions.tail)
  }
}
