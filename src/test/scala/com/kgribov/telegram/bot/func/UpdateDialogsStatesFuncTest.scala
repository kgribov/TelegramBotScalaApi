package com.kgribov.telegram.bot.func

import java.time.{LocalDateTime, ZonedDateTime}

import com.kgribov.telegram.bot.dialog.{Dialog, TextQuestion}
import com.kgribov.telegram.bot.func.bot.UpdateDialogsStatesFunc
import com.kgribov.telegram.bot.loader.TelUpdate
import com.kgribov.telegram.bot.model._
import com.kgribov.telegram.bot.state.{BotState, DialogState, QuestionState}
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.duration._

class UpdateDialogsStatesFuncTest extends FunSuite with Matchers {

  private val successAlert = "Dialog finished"

  test("should ask first question") {
    val updateDialogsFunc = new UpdateDialogsStatesFunc

    val questionToAsk = "who let the dog out?"
    val chat = Chat("1", None, None, "private")
    val dialog = Dialog(submitAnswers = _ => successAlert)

    val botState = BotState(dialogsStates = Seq(
      DialogState(
        dialogId = chat.id,
        chat = chat,
        dialog = dialog,
        notDoneQuestions = Seq(
          QuestionState(
            question = new TextQuestion(
              question = questionToAsk
            )
          )
        ),
        doneQuestions = Seq.empty,
        expiredAt = LocalDateTime.now().plusHours(1)
      )
    ))

    val (state, reply) = updateDialogsFunc(botState, TelUpdate())

    reply.textMessages should have size 1
    reply.textMessages.head should be (BotTextReply("1", questionToAsk))

    val dialogState = state.dialogsStates.head
    dialogState.notDoneQuestions should have size 1
    dialogState.notDoneQuestions.head.askedAt.isDefined should be (true)
  }

  test("should ask second question on answer for first question") {
    val updateDialogsFunc = new UpdateDialogsStatesFunc

    val questionToAsk = "who let the dog out?"
    val chat = Chat("1", None, None, "private")
    val dialog = Dialog(submitAnswers = _ => successAlert)

    val botState = BotState(dialogsStates = Seq(
      DialogState(
        dialogId = chat.id,
        chat = chat,
        dialog = dialog,
        notDoneQuestions = Seq(
          QuestionState(
            question = new TextQuestion(
              question = "What is a capital of USA?"
            ),
            askedAt = Some(LocalDateTime.now().minusMinutes(1))
          ),
          QuestionState(
            question = new TextQuestion(
              question = questionToAsk
            )
          )
        ),
        doneQuestions = Seq.empty,
        expiredAt = LocalDateTime.now().plusHours(1)
      )
    ))

    val update = TelUpdate(textMessages = Seq(
      TextMessage(
        id = "1",
        text = "Washington",
        from = testUser(1),
        replyTo = None,
        date = ZonedDateTime.now(),
        chat = testPrivateChat(chat.id))
    ))

    val (state, reply) = updateDialogsFunc(botState, update)

    reply.textMessages should have size 1
    reply.textMessages.head should be (BotTextReply("1", questionToAsk))

    state.dialogsStates should have size 1

    val dialogState = state.dialogsStates.head
    dialogState.notDoneQuestions should have size 1
    dialogState.notDoneQuestions.head.askedAt.isDefined should be (true)

    dialogState.doneQuestions should have size 1
  }

  test("should remove dialog on completed questions") {
    val updateDialogsFunc = new UpdateDialogsStatesFunc

    val questionToAsk = "who let the dog out?"
    val chat = Chat("1", None, None, "private")
    val dialog = Dialog(submitAnswers = _ => successAlert)

    val botState = BotState(dialogsStates = Seq(
      DialogState(
        dialogId = chat.id,
        chat = chat,
        dialog = dialog,
        notDoneQuestions = Seq(
          QuestionState(
            question = new TextQuestion(
              question = questionToAsk
            ),
            askedAt = Some(LocalDateTime.now())
          )
        ),
        doneQuestions = Seq.empty,
        expiredAt = LocalDateTime.now().plusHours(1)
      )
    ))

    val update = TelUpdate(textMessages = Seq(
      TextMessage(
        id = "1",
        text = "off off off",
        from = testUser(1),
        replyTo = None,
        date = ZonedDateTime.now(),
        chat = testPrivateChat(chat.id))
    ))

    val (state, reply) = updateDialogsFunc(botState, update)

    reply.textMessages should have size 1
    reply.textMessages.head.text should be (successAlert)

    state.dialogsStates should have size 0
  }

  test("should not ask next question, if previous is alive with ttl") {
    val updateDialogsFunc = new UpdateDialogsStatesFunc

    val questionToAsk = "who let the dog out?"
    val answer = "oof oof oof"
    val chat = Chat("1", None, None, "private")
    val dialog = Dialog(submitAnswers = _ => successAlert)

    val botState = BotState(dialogsStates = Seq(
      DialogState(
        dialogId = chat.id,
        chat = chat,
        dialog = dialog,
        notDoneQuestions = Seq(
          QuestionState(
            question = new TextQuestion(
              question = questionToAsk,
              questionTTL = Some(1.hour)
            ),
            askedAt = Some(LocalDateTime.now())
          ),
          QuestionState(
            question = new TextQuestion(
              question = "Next question"
            )
          )
        ),
        doneQuestions = Seq.empty,
        expiredAt = LocalDateTime.now().plusHours(1)
      )
    ))

    val update = TelUpdate(textMessages = Seq(
      TextMessage(
        id = "1",
        text = answer,
        from = testUser(1),
        replyTo = None,
        date = ZonedDateTime.now(),
        chat = testPrivateChat(chat.id))
    ))

    val (state, reply) = updateDialogsFunc(botState, update)

    reply.textMessages should have size 0

    val dialogState = state.dialogsStates.head
    dialogState.notDoneQuestions should have size 2
    dialogState.notDoneQuestions.head.askedAt.isDefined should be (true)

    dialogState.notDoneQuestions.head.answers.head.answer should be (answer)

    dialogState.doneQuestions should have size 0
  }

  test("should collect answers from one user only and ignore another chats") {
    val updateDialogsFunc = new UpdateDialogsStatesFunc

    val questionToAsk = "who let the dog out?"
    val answer = "oof oof oof"
    val chat = Chat("1", None, None, "private")
    val dialog = Dialog(submitAnswers = _ => successAlert)
    val forUserOnlyId = 1

    val botState = BotState(dialogsStates = Seq(
      DialogState(
        dialogId = chat.id,
        chat = chat,
        dialog = dialog,
        notDoneQuestions = Seq(
          QuestionState(
            question = new TextQuestion(
              question = questionToAsk,
              questionTTL = Some(1.hour),
              withOnlyOneUser = true
            ),
            askedAt = Some(LocalDateTime.now()),
            withUserOnlyId = Some(forUserOnlyId)
          )
        ),
        doneQuestions = Seq.empty,
        expiredAt = LocalDateTime.now().plusHours(1)
      )
    ))

    val update = TelUpdate(textMessages = Seq(
      TextMessage(
        id = "1",
        text = answer,
        from = testUser(forUserOnlyId),
        replyTo = None,
        date = ZonedDateTime.now(),
        chat = testPrivateChat(chat.id)
      ),
      TextMessage(
        id = "2",
        text = "Another answer",
        from = testUser(2),
        replyTo = None,
        date = ZonedDateTime.now(),
        chat = testPrivateChat(chat.id)
      ),
      TextMessage(
        id = "3",
        text = "From wrong chat",
        from = testUser(3),
        replyTo = None,
        date = ZonedDateTime.now(),
        chat = testPrivateChat("another chat id")
      )
    ))

    val (state, reply) = updateDialogsFunc(botState, update)

    reply.textMessages should have size 0

    val dialogState = state.dialogsStates.head
    dialogState.notDoneQuestions should have size 1
    dialogState.notDoneQuestions.head.askedAt.isDefined should be (true)

    dialogState.notDoneQuestions.head.answers should have size 1
    dialogState.notDoneQuestions.head.answers.head.answer should be (answer)

    dialogState.doneQuestions should have size 0
  }

  private def testUser(userId: Long): User = {
    User(userId, isBot = false, "test", None, None)
  }

  private def testPrivateChat(chatId: String): Chat = {
    Chat(chatId, None, None, "private")
  }
}
