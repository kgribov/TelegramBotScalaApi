package com.kgribov.telegram.bot

import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}
import scalaj.http.{HttpRequest, HttpResponse}

package object http extends LazyLogging {

  def requestForResponse(request: HttpRequest, retries: Int, tryCount: Int = 0): HttpResponse[String] = {
    val responseTry = Try(request.asString)
    responseTry match {
      case Success(response) => response
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

  def requestForTextResponse(request: HttpRequest, retries: Int, tryCount: Int = 0): String = {
    requestForResponse(request, retries, tryCount).body
  }

  class UnableToSendRequest(message: String, exception: Throwable) extends Exception(message, exception)
}
