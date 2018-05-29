package com.kgribov.telegram.bot.loader

import com.kgribov.telegram.bot.endpoints.TelegramEndpoints
import com.kgribov.telegram.bot.http.requestForResponse
import com.kgribov.telegram.bot.parser.parseUpdates
import com.typesafe.scalalogging.LazyLogging

import scalaj.http.Http

class TelUpdateLoader(apiKey: String,
                      timeoutInSec: Int = 20,
                      limit: Int = 50) extends LazyLogging {

  def loadUpdateFromOffset(offset: Long): (TelUpdate, Long) = {
    val request = Http(TelegramEndpoints.updatesUrl(apiKey))
      .param("offset", offset.toString)
      .param("timeout", timeoutInSec.toString)
      .param("limit", limit.toString)
      .timeout(connTimeoutMs = 3*1000, readTimeoutMs = 60*1000)

    val response = requestForResponse(request, 100)
    if (response.isError) {
      throw new UnableToGetUpdates(response.code)
    }

    val responseBody = response.body
    logger.debug(s"Get next response: [$responseBody]")

    val (updates, updatesIds) = parseUpdates(responseBody)
      .map(_.toModel)
      .unzip

    (mergeUpdates(updates), if (updatesIds.isEmpty) offset else updatesIds.max + 1)
  }

  private def mergeUpdates(updates: Seq[TelUpdate]): TelUpdate = {
    updates.fold(TelUpdate())((update1, update2) => update1.merge(update2))
  }
}

class UnableToGetUpdates(returnCode: Int)
  extends Exception(s"Unable to get updates from Telegram server. Return code [$returnCode]")
