package com.kgribov.telegram.bot.schema

import com.kgribov.telegram.bot.func.UserBotFunction
import com.kgribov.telegram.bot.func.user.OnTextReplyTextFunc
import com.kgribov.telegram.bot.model.TextMessage

case class MessageProcessSchema(process: TextMessage => Option[String],
                                filter: TextMessage => Boolean = _ => false) {

  def toBotFunc: UserBotFunction = {
    def processWithFilter(message: TextMessage): Option[String] = {
      if (filter(message)) {
        process(message)
      } else {
        None
      }
    }
    new OnTextReplyTextFunc(processWithFilter)
  }
}
