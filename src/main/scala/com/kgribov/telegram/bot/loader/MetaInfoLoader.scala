package com.kgribov.telegram.bot.loader

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.kgribov.telegram.bot.endpoints.TelegramEndpoints
import com.kgribov.telegram.bot.model.User
import com.kgribov.telegram.bot.parser.parseAdminsResponse
import com.kgribov.telegram.bot.http.requestForTextResponse

import scala.concurrent.duration._
import scalaj.http.Http

class MetaInfoLoader(apiKey: String) {

  private val cache: LoadingCache[String, List[User]] =
    Scaffeine()
      .recordStats()
      .expireAfterWrite(1.hour)
      .maximumSize(500)
      .build((chatId: String) => loadAdmins(chatId))

  def loadAdministrators(chatId: String): List[User] = {
    cache.get(chatId)
  }

  private def loadAdmins(chatId: String): List[User] = {
    val request = Http(TelegramEndpoints.chatAdministratorsUrl(apiKey))
      .postForm(Seq(("chat_id", chatId.toString)))
    val response = requestForTextResponse(request, 100)
    parseAdminsResponse(response).map(_.toModel)
  }
}
