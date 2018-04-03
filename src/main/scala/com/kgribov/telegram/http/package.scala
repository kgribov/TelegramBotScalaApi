package com.kgribov.telegram

import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}
import scalaj.http.HttpRequest

package object http extends LazyLogging {

  def requestForResponse(request: HttpRequest, retries: Int, tryCount: Int = 0): String = {
    val response = Try(request.asString.body)
    response match {
      case Success(body) => body
      case Failure(ex) => {
        logger.error(s"Unable to send request $request. Try count: $tryCount", ex)
        if (tryCount == retries) {
          throw new UnableToSendRequest("Max retries is reached for send request", ex)
        } else {
          requestForResponse(request, retries, tryCount + 1)
        }
      }
    }
  }

  class UnableToSendRequest(message: String, exception: Throwable) extends Exception(message, exception)
}
