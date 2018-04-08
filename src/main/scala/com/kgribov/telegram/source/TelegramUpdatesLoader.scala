package com.kgribov.telegram.source

import com.kgribov.telegram.endpoints.TelegramEndpoints
import com.kgribov.telegram.model.Update
import com.kgribov.telegram.parser._
import com.typesafe.scalalogging.LazyLogging

import com.kgribov.telegram.http._

import scalaj.http.Http

class TelegramUpdatesLoader(apiKey: String) extends LazyLogging {

  def loadUpdates(fromOffset: Long): List[Update] = {
    val request = Http(TelegramEndpoints.updatesUrl(apiKey))
      .param("offset", fromOffset.toString)
      .param("timeout", "20")
      .param("limit", "50")
      .timeout(connTimeoutMs = 3*1000, readTimeoutMs = 60*1000)

    val response = requestForResponse(request, 100)
    if (response.isError) {
      throw new UnableToGetUpdates(response.code)
    }

    val responseBody = response.body
    logger.debug(s"Get next response: [$responseBody]")

    parseUpdates(responseBody).map(_.toModel)
  }
}

class UnableToGetUpdates(returnCode: Int)
  extends Exception(s"Unable to get updates from Telegram server. Return code [$returnCode]")
