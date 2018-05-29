package com.kgribov.telegram.bot.func.bot

import com.kgribov.telegram.bot.func.FilterBotFunction
import com.kgribov.telegram.bot.loader.{MetaInfoLoader, TelUpdate}
import com.kgribov.telegram.bot.model.{BotTextReply, CommandMessage}
import com.kgribov.telegram.bot.security.{ChatPermission, MultiPermissions}
import com.kgribov.telegram.bot.sender.TelBotReply

class CommandPermissionFilterFunc(commandsPermissions: Map[String, ChatPermission],
                                  metaInfoLoader: MetaInfoLoader) extends FilterBotFunction {

  override def apply(update: TelUpdate): (TelUpdate, TelBotReply) = {
    val filteredCommands = update
      .commandMessages
      .filter(commandMessage =>
        permission(commandMessage.command).isAllowed(commandMessage, metaInfoLoader)
      )

    val replies = update
      .commandMessages
      .filterNot(commandMessage =>
        permission(commandMessage.command).isAllowed(commandMessage, metaInfoLoader)
      )
      .map(commandMessage => permissionsAlertMessage(commandMessage))

    (
      update.copy(commandMessages = filteredCommands),
      TelBotReply(textMessages = replies)
    )
  }

  private def permission(command: String): ChatPermission = {
    commandsPermissions
      .getOrElse(command, new MultiPermissions())
  }

  private def permissionsAlertMessage(commandMessage: CommandMessage): BotTextReply = {
    val alertText = permission(commandMessage.command)
      .permissionsMessage(commandMessage.command, commandMessage.from.firstName)
    BotTextReply(commandMessage.chat.id, alertText)
  }
}
