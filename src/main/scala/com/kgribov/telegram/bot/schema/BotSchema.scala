package com.kgribov.telegram.bot.schema

import com.kgribov.telegram.bot.BotRunner
import com.kgribov.telegram.bot.func.bot.CommandPermissionFilterFunc
import com.kgribov.telegram.bot.loader.MetaInfoLoader
import com.kgribov.telegram.bot.model._
import com.kgribov.telegram.bot.security.ChatPermission
import com.kgribov.telegram.bot.state.StateStore

case class BotSchema private[schema] (messageProcessSchemas: Seq[MessageProcessSchema] = Seq.empty,
                                      commandProcessSchemas: Seq[(String, CommandProcessSchema)] = Seq.empty,
                                      dialogsProcessSchemas: Seq[(String, DialogSchema)] = Seq.empty,
                                      commandsPermissions: Map[String, ChatPermission] = Map.empty) {

  def onMessage(process: TextMessage => Unit): BotSchema = {
    def processToUnit(message: TextMessage): Option[String] = {
      process(message)
      None
    }
    val schema = MessageProcessSchema(process = processToUnit)
    this.copy(messageProcessSchemas = schema +: messageProcessSchemas)
  }

  def replyOnMessage(filter: TextMessage => Boolean = _ => true,
                     reply: TextMessage => String): BotSchema = {
    def processToString(message: TextMessage): Option[String] = {
      Some(reply(message))
    }
    val schema = MessageProcessSchema(process = processToString, filter = filter)
    this.copy(messageProcessSchemas = schema +: messageProcessSchemas)
  }

  def onCommand(command: String,
                process: CommandMessage => Unit,
                withPermissions: ChatPermission = allowEverything()): BotSchema = {

    def processToUnit(message: CommandMessage): Option[String] = {
      process(message)
      None
    }
    val schema = CommandProcessSchema(processToUnit)
    this.copy(
      commandProcessSchemas = (command, schema) +: commandProcessSchemas,
      commandsPermissions = commandsPermissions + (command -> withPermissions)
    )
  }

  def replyOnCommand(command: String,
                     process: CommandMessage => String,
                     withPermissions: ChatPermission = allowEverything()): BotSchema = {

    def processToString(message: CommandMessage): Option[String] = {
      Some(process(message))
    }
    val schema = CommandProcessSchema(processToString)
    this.copy(
      commandProcessSchemas = (command, schema) +: commandProcessSchemas,
      commandsPermissions = commandsPermissions + (command -> withPermissions)
    )
  }

  def startDialogOnCommand(command: String,
                           dialog: DialogSchema,
                           withPermissions: ChatPermission = allowEverything()): BotSchema = {
    this.copy(
      dialogsProcessSchemas = (command -> dialog) +: dialogsProcessSchemas,
      commandsPermissions = commandsPermissions + (command -> withPermissions)
    )
  }


  def startBot(apiKey: String,
               stateStorePath: String = s"${System.getProperty("user.home")}/telegram_bot"): Unit = {

    val botFunctions =
      messageProcessSchemas.map(_.toBotFunc) ++
      commandProcessSchemas.map{ case (command, messageSchema) => messageSchema.toBotFunc(command) } ++
      dialogsProcessSchemas.map{ case (command, dialogSchema) => dialogSchema.toBotFunc(command) }

    val filterFunctions = Seq(
      new CommandPermissionFilterFunc(commandsPermissions, new MetaInfoLoader(apiKey))
    )

    val stateStore = new StateStore(apiKey)

    val botRunner = new BotRunner(
      apiKey,
      botFunctions,
      filterFunctions,
      stateStore
    )

    botRunner.run()
  }
}
