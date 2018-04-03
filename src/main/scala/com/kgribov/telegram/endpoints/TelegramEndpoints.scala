package com.kgribov.telegram.endpoints

object TelegramEndpoints {

  val TELEGRAM_HOST = "https://api.telegram.org"

  def botHostName(apiKey: String): String = s"$TELEGRAM_HOST/bot$apiKey/"

  def updatesUrl(apiKey: String): String = botHostName(apiKey) + "getUpdates"

  def sendMessageUrl(apiKey: String): String = botHostName(apiKey) + "sendMessage"

  def answerCallbackUrl(apiKey: String): String = botHostName(apiKey) + "answerCallbackQuery"

  def chatAdministratorsUrl(apiKey: String): String = botHostName(apiKey) + "getChatAdministrators"
}
