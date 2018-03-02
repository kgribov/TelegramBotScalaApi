package com.kgribov.telegram.process

import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit

import com.kgribov.telegram.model.{Chat, Message, MessageToSend, User}
import com.kgribov.telegram.sender.MessageSender
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.duration.Duration

class DialogProcessorTest extends FunSuite with Matchers with MockFactory {

  private val chatId = 1
  private val withUser = Some(User(1, false, "TestName", None, None))

  test("should ask question if it was not asked") {
    val questionText = "is test works?"
    val sender = mock[MessageSender]
    val processor = new DialogProcessor(
      chatId = chatId,
      messageSender = sender,
      questions = List(askTextQuestion(questionText, sender)),
      withUserOnly = withUser
    )

    (sender.send _).expects(MessageToSend(chatId, questionText)).returning(message(chatId))

    processor.processMessages(None)
  }

  test("should not be active if ttl is reached") {
    val questionText = "is test expired?"
    val sender = stub[MessageSender]
    val processor = new DialogProcessor(
      chatId = chatId,
      messageSender = sender,
      questions = List(askTextQuestion(questionText, sender)),
      withUserOnly = withUser,
      dialogTimeout = Duration(1, TimeUnit.MINUTES),
      now = ZonedDateTime.now().minusMinutes(2)
    )

    processor.processMessages(None)

    (sender.send _).verify(MessageToSend(chatId, questionText)).never
  }

  test("should move to next question after answer") {
    val questionOneText = "it is first question"
    val questionSecondText = "it is second question"
    val sender = mock[MessageSender]
    val processor = new DialogProcessor(
      chatId = chatId,
      messageSender = sender,
      questions = List(
        askTextQuestion(questionOneText, sender),
        askTextQuestion(questionSecondText, sender)
      ),
      withUserOnly = withUser
    )

    (sender.send _).expects(MessageToSend(chatId, questionOneText)).returning(message(chatId))
    processor.processMessages(None)

    processor.processMessages(Some(List(message(chatId))))

    (sender.send _).expects(MessageToSend(chatId, questionSecondText)).returning(message(chatId))
    processor.processMessages(None)
  }

  private def message(chatId: Int, user: User = withUser.get): Message = Message(
    UUID.randomUUID().toString,
    user,
    None,
    None,
    false,
    ZonedDateTime.now(),
    Chat(chatId, None, None, "test"),
    ""
  )

  private def askTextQuestion(text: String, messageSender: MessageSender): Function[DialogAnswers, DialogQuestion] =
    (_: DialogAnswers) => TextDialogQuestion(messageSender, text)
}
