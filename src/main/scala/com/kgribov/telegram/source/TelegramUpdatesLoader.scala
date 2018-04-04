package com.kgribov.telegram.source

import com.kgribov.telegram.endpoints.TelegramEndpoints
import com.kgribov.telegram.model.Update
import com.kgribov.telegram.parser._
import com.typesafe.scalalogging.LazyLogging

import scalaj.http.Http

class TelegramUpdatesLoader(apiKey: String) extends LazyLogging {

  def loadUpdates(fromOffset: Int): List[Update] = {
    val response = Http(TelegramEndpoints.updatesUrl(apiKey))
      .param("offset", fromOffset.toString)
      .timeout(connTimeoutMs = 10000, readTimeoutMs = 10000)
      .asString

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
