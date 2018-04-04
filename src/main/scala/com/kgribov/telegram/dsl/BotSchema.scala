package com.kgribov.telegram.dsl

import com.kgribov.telegram.bot.{Bot, StopBot, StopBotOnSignal}
import com.kgribov.telegram.model.{Message, User}
import com.kgribov.telegram.process.{DialogProcessor, MessageProcessor}
import com.kgribov.telegram.security._
import com.kgribov.telegram.sender.MessageSender
import com.kgribov.telegram.source._

class BotSchema(apiKey: String,
                botName: String,
                processAnyMessage: List[Message => Option[String]] = List(),
                simpleCommandsProcessors: Map[String, Message => Option[String]] = Map(),
                dialogsProcessors: Map[String, Message => DialogProcessor] = Map(),
                commandsPermissions: Map[String, ChatPermission] = Map()) {

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
      botName,
      processFun :: processAnyMessage,
      simpleCommandsProcessors,
      dialogsProcessors,
      commandsPermissions
    )
  }

  def replyOnMessage(filter: Message => Boolean = _ => true, reply: Message => String): BotSchema = {
    val processFun = new Function[Message, Option[String]] {
      override def apply(message: Message) = {
        if (filter(message)) {
          Some(reply(message))
        } else {
          None
        }
      }
    }

    new BotSchema(
      apiKey,
      botName,
      processFun :: processAnyMessage,
      simpleCommandsProcessors,
      dialogsProcessors,
      commandsPermissions
    )
  }

  def onCommand(command: String, process: Message => Unit, withPermissions: ChatPermission = allowEverything()): BotSchema = {
    val processFun = new Function[Message, Option[String]] {
      override def apply(message: Message) = {
        process(message)
        None
      }
    }

    new BotSchema(
      apiKey,
      botName,
      processAnyMessage,
      simpleCommandsProcessors + (command -> processFun),
      dialogsProcessors,
      commandsPermissions + (command -> withPermissions)
    )
  }

  def replyOnCommand(command: String, process: Message => String, withPermissions: ChatPermission = allowEverything()): BotSchema = {
    val processFun = new Function[Message, Option[String]] {
      override def apply(message: Message) = {
        Some(process(message))
      }
    }

    new BotSchema(
      apiKey,
      botName,
      processAnyMessage,
      simpleCommandsProcessors + (command -> processFun),
      dialogsProcessors,
      commandsPermissions + (command -> withPermissions)
    )
  }

  def startDialogOnCommand(command: String, dialog: Dialog, withPermissions: ChatPermission = allowEverything()): BotSchema = {
    val dialogProcessor = dialogToProcessor(dialog)
    new BotSchema(
      apiKey,
      botName,
      processAnyMessage,
      simpleCommandsProcessors,
      dialogsProcessors + (command -> dialogProcessor),
      commandsPermissions + (command -> withPermissions)
    )
  }

  private def createMessageProcessor(): MessageProcessor = {
    new MessageProcessor(
      processAnyMessage,
      simpleCommandsProcessors,
      dialogsProcessors,
      commandsPermissions,
      new MetaInfoSource(apiKey),
      messageSender
    )
  }

  private def dialogToProcessor(dialog: Dialog): Message => DialogProcessor = {
    (message: Message) => {
      new DialogProcessor(
        message.chat.id,
        dialog.dialogTTL,
        messageSender,
        dialog.questions.map(_.toQuestion(messageSender)),
        dialogWithUser(message.from, dialog.personalDialog)
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

  def startBot(stopBot: StopBot = new StopBotOnSignal()): Unit = {
    val messageSource = new MessagesSource(
      new TelegramUpdatesLoader(apiKey).loadUpdates,
      new FileBasedOffsetStore(botName)
    )

    val bot = new Bot(messageSource, createMessageProcessor())

    bot.start(stopBot)
  }
}
