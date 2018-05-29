package com.kgribov.telegram.bot.schema

import com.kgribov.telegram.bot.func.UserBotFunction
import com.kgribov.telegram.bot.func.user.OnCommandReplyTextFunc
import com.kgribov.telegram.bot.model.CommandMessage

case class CommandProcessSchema(process: CommandMessage => Option[String]) {

  def toBotFunc(command: String): UserBotFunction = {
    new OnCommandReplyTextFunc(command, process)
  }
}
