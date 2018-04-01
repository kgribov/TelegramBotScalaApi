package com.kgribov.telegram.process

import java.time.Clock

import com.kgribov.telegram.model.{Message, MessageToSend}
import com.kgribov.telegram.sender.MessageSender
import com.typesafe.scalalogging.LazyLogging

class MessageProcessor(anyMessageProcessors: List[Message => Option[String]],
                       simpleCommandsProcessors: Map[String, Message => Option[String]],
                       dialogsProcessors: Map[String, Message => DialogProcessor],
                       messageSender: MessageSender) extends LazyLogging {

  private val dialogPool = new DialogPool(messageSender, Clock.systemDefaultZone())

  def processMessages(messages: List[Message]): Unit = {
    processAnyMessage(messages)
    processCommands(messages)
    processDialogs(messages)
  }

  private def processAnyMessage(messages: List[Message]): Unit = {
    logger.info(s"Going to process ${messages.size} messages")
    val replyMessages = messages.flatMap(message => {
      anyMessageProcessors.map(processFun => {
        processFun(message)
          .map(replyText => MessageToSend(message.chat.id, replyText))
      })
    }).flatten
    messageSender.sendMessages(replyMessages)
  }

  private def processCommands(messages: List[Message]): Unit = {
    val commandsOnly = messages.filter(_.command.isDefined)
    logger.info(s"Going to process ${commandsOnly.size} messages with commands")
    val replyMessages = commandsOnly
      .flatMap(message => {
        val command = message.command.get
        simpleCommandsProcessors
          .get(command)
          .map(_.apply(message))
          .flatMap(reply => reply.map(replyText => MessageToSend(message.chat.id, replyText)))
    })
    messageSender.sendMessages(replyMessages)
  }

  private def processDialogs(messages: List[Message]): Unit = {
    createDialogs(messages)
    dialogPool.processMessages(messages)
  }

  private def createDialogs(messages: List[Message]): Unit = {
    val messageToCommand = messages
      .filter(_.command.isDefined)
      .filter(message => dialogsProcessors.contains(message.command.get))
      .groupBy(_.command.get)

    messageToCommand.foreach {
      case (command, messagesOfCommand) => messagesOfCommand.foreach(message => {
        dialogPool.addDialog(message.chat.id, dialogsProcessors(command)(message))
      })
    }
  }
}
