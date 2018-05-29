package com.kgribov.telegram.examples

import com.kgribov.telegram.bot.schema._
import com.kgribov.telegram.bot.security._

object CommandsPermissionsExample extends App {

  val apiKey = ""

  def botSchema(): BotSchema = {
    createBotSchema()
      .replyOnCommand("groupAdminAction", _ => "ADMIN IS HERE", withPermissions = allowGroups(ADMIN_ONLY))

      .replyOnCommand("groupAction", _ => "Hi chat!", withPermissions = allowGroups())

      .replyOnCommand("privateAction", _ => "Hi man!", withPermissions = allowPrivateChats())

      .replyOnCommand("id", message => s"Your id = [${message.from.id}], chat id = [${message.chat.id}]")
  }

  botSchema().startBot(apiKey)
}
