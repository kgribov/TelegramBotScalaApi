package com.kgribov.telegram.process

import java.time.ZonedDateTime
import java.util.UUID

import com.kgribov.telegram.model._
import com.kgribov.telegram.sender.MessageSender
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSuite, Matchers}

class KeyboardDialogQuestionTest extends FunSuite with Matchers with MockFactory {

  private val user = User(1, false, "UserName", None, None)

  test("KeyboardDialogQuestion should send success alert on answer") {
    val sender = mock[MessageSender]
    val chatId = 1
    val questionText = "success keyboard question"
    val successAlert = "Success"
    val question = KeyboardDialogQuestion(sender, questionText, List("answer1", "answer2"), _ => successAlert)

    val replyMessage = message(chatId)
    (sender.send _).expects(where {
      (message: MessageToSend) => message.replyKeyboard.isDefined
    }).returning(replyMessage)

    (sender.sendKeyboardAlert _).expects(where {
      (alert: KeyboardAlert) => !alert.showAlert && alert.text == successAlert
    })

    question.askQuestion(chatId)

    question.processReply(message(chatId, text = "answer1", replyTo = Some(replyMessage)))
  }

  test("KeyboardDialogQuestion should send fail alert on repeated answer") {
    val sender = mock[MessageSender]
    val chatId = 1
    val questionText = "success keyboard question"
    val successAlert = "Success"
    val question = KeyboardDialogQuestion(sender, questionText, List("answer1", "answer2"), _ => successAlert)

    val replyMessage = message(chatId)
    (sender.send _).expects(where {
      (message: MessageToSend) => message.replyKeyboard.isDefined
    }).returning(replyMessage)

    (sender.sendKeyboardAlert _).expects(where {
      (alert: KeyboardAlert) => !alert.showAlert && alert.text == successAlert
    })

    (sender.sendKeyboardAlert _).expects(where {
      (alert: KeyboardAlert) => alert.showAlert
    })

    question.askQuestion(chatId)

    question.processReply(message(chatId, text = "answer1", replyTo = Some(replyMessage)))

    question.processReply(message(chatId, text = "answer2", replyTo = Some(replyMessage)))
  }

  test("KeyboardDialogQuestion should returns answers on ttl") {
    val sender = mock[MessageSender]
    val chatId = 1
    val questionText = "success keyboard question"
    val successAlert = "Success"
    val questionExpiredAt = Some(ZonedDateTime.now().minusMinutes(1))
    val questionIsNotExpiredAt = ZonedDateTime.now().minusMinutes(2)

    val question = KeyboardDialogQuestion(
      sender,
      questionText,
      List("answer1", "answer2"),
      _ => successAlert,
      "you have already answered",
      questionExpiredAt
    )

    val replyMessage = message(chatId)
    (sender.send _).expects(where {
      (message: MessageToSend) => message.replyKeyboard.isDefined
    }).returning(replyMessage)

    (sender.sendKeyboardAlert _).expects(where {
      (alert: KeyboardAlert) => !alert.showAlert && alert.text == successAlert
    })

    question.askQuestion(chatId)

    val answer = message(chatId, text = "answer1", replyTo = Some(replyMessage))
    question.processReply(answer)

    // answers is ready
    question.isDone() should be (Some(Answer(List(answer))))

    // answers not ready due to ttl
    question.isDone(questionIsNotExpiredAt) should be (None)
  }

  private def message(chatId: Int,
                      replyTo: Option[Message] = None,
                      user: User = user,
                      text: String = ""): Message = Message(
    UUID.randomUUID().toString,
    user,
    None,
    replyTo,
    true,
    ZonedDateTime.now(),
    Chat(chatId, None, None, "test"),
    text
  )
}
