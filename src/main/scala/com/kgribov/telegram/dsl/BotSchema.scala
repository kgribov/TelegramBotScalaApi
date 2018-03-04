package com.kgribov.telegram.dsl

import com.kgribov.telegram.bot.{Bot, StopBot, StopBotOnSignal}
import com.kgribov.telegram.model.{Message, User}
import com.kgribov.telegram.process.{DialogProcessor, MessageProcessor}
import com.kgribov.telegram.sender.MessageSender
import com.kgribov.telegram.source.{FileBasedOffsetStore, InMemoryOffsetStore, MessagesSource, TelegramUpdatesLoader}

class BotSchema(apiKey: String,
                processAnyMessage: List[Message => Option[String]] = List(),
                simpleCommandsProcessors: Map[String, Message => Option[String]] = Map(),
                dialogsProcessors: Map[String, Message => DialogProcessor] = Map()) {

  val messageSender = new MessageSender(apiKey)

  def onMessage(process: Message => Unit): BotSchema = {
    val processFun = new Function[Message, Option[String]] {
      override def apply(message: Message) = {
        process(message)
        None
      }
    }

    new BotSchema(
      apiKey,
      processFun :: processAnyMessage,
      simpleCommandsProcessors,
      dialogsProcessors
    )
  }

  def replyOnMessage(process: Message => String): BotSchema = {
    val processFun = new Function[Message, Option[String]] {
      override def apply(message: Message) = {
        Some(process(message))
      }
    }

    new BotSchema(
      apiKey,
      processFun :: processAnyMessage,
      simpleCommandsProcessors,
      dialogsProcessors
    )
  }

  def onCommand(command: String, process: Message => Unit): BotSchema = {
    val processFun = new Function[Message, Option[String]] {
      override def apply(message: Message) = {
        process(message)
        None
      }
    }

    new BotSchema(
      apiKey,
      processAnyMessage,
      simpleCommandsProcessors + (command -> processFun),
      dialogsProcessors
    )
  }

  def replyOnCommand(command: String, process: Message => String): BotSchema = {
    val processFun = new Function[Message, Option[String]] {
      override def apply(message: Message) = {
        Some(process(message))
      }
    }

    new BotSchema(
      apiKey,
      processAnyMessage,
      simpleCommandsProcessors + (command -> processFun),
      dialogsProcessors
    )
  }

  def startDialogOnCommand(command: String, dialog: AskDialog): BotSchema = {
    val dialogProcessor = dialogToProcessor(dialog)
    new BotSchema(
      apiKey,
      processAnyMessage,
      simpleCommandsProcessors,
      dialogsProcessors + (command -> dialogProcessor)
    )
  }

  private def createMessageProcessor(): MessageProcessor = {
    new MessageProcessor(
      processAnyMessage,
      simpleCommandsProcessors,
      dialogsProcessors,
      messageSender
    )
  }

  private def dialogToProcessor(dialog: AskDialog): Message => DialogProcessor = {
    (message: Message) => {
      new DialogProcessor(
        message.chat.id,
        dialog.dialogTTL,
        messageSender,
        dialog.questions.toList.map(_.toQuestion(messageSender)),
        dialogWithUser(message.from, dialog.withUserOnly)
      )
    }
  }

  private def dialogWithUser(user: User, withThisUser: Boolean): Option[User] = {
    if (withThisUser) {
      Some(user)
    } else {
      None
    }
  }

  def startBot(stopBot: StopBot = new StopBotOnSignal(),
               withId: String = apiKey.split(":")(0)): Unit = {
    val messageSource = new MessagesSource(
      new TelegramUpdatesLoader(apiKey).loadUpdates,
      new FileBasedOffsetStore(withId)
    )

    val bot = new Bot(messageSource, createMessageProcessor())

    bot.start(stopBot)
  }
}
