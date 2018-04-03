package com.kgribov.telegram.source

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.kgribov.telegram.endpoints.TelegramEndpoints
import com.kgribov.telegram.model.User
import com.kgribov.telegram.http._
import com.kgribov.telegram.parser._

import scala.concurrent.duration._
import scalaj.http.Http

class MetaInfoSource(apiKey: String) {

  private val cache: LoadingCache[Int, List[User]] =
    Scaffeine()
      .recordStats()
      .expireAfterWrite(1.hour)
      .maximumSize(500)
      .build((chatId: Int) => loadAdmins(chatId))

  def getChatAdministrators(chatId: Int): List[User] = {
    cache.get(chatId)
  }

  private def loadAdmins(chatId: Int): List[User] = {
    val request = Http(TelegramEndpoints.chatAdministratorsUrl(apiKey))
      .postForm(Seq(("chat_id", chatId.toString)))
    val response = requestForResponse(request, 100)
    parseAdminsResponse(response).map(_.toModel)
  }
}
