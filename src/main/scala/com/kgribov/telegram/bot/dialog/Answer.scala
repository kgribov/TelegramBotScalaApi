package com.kgribov.telegram.bot.dialog

import com.kgribov.telegram.bot.model.User

case class Answer(messageId: String, fromUser: User, fromChatId: String, answer: String)
