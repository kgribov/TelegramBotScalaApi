package com.kgribov.telegram.source

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.kgribov.telegram.endpoints.TelegramEndpoints
import com.kgribov.telegram.model.User
import com.kgribov.telegram.http._
import com.kgribov.telegram.parser._

import scala.concurrent.duration._
import scalaj.http.Http

class MetaInfoSource(apiKey: String) {

  private val cache: LoadingCache[Long, List[User]] =
    Scaffeine()
      .recordStats()
      .expireAfterWrite(1.hour)
      .maximumSize(500)
      .build((chatId: Long) => loadAdmins(chatId))

  def getChatAdministrators(chatId: Long): List[User] = {
    cache.get(chatId)
  }

  private def loadAdmins(chatId: Long): List[User] = {
    val request = Http(TelegramEndpoints.chatAdministratorsUrl(apiKey))
      .postForm(Seq(("chat_id", chatId.toString)))
    val response = requestForTextResponse(request, 100)
    parseAdminsResponse(response).map(_.toModel)
  }
}
