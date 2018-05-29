package com.kgribov.telegram.bot.sender

case class SendStatistic(sentTextReplies: Int = 0,
                         sentKeyboardReplies: Int = 0,
                         sentKeyboardAlerts: Int = 0)
