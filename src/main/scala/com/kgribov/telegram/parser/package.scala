package com.kgribov.telegram

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.kgribov.telegram.json.{InlineKeyboardMarkup, MessageJson, UpdateJson}
import com.kgribov.telegram.model.Keyboard
import com.typesafe.scalalogging.LazyLogging

package object parser extends LazyLogging {

  private val mapper = {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper
  }

  def parseUpdates(json: String): List[UpdateJson] = {
    val response = mapper.readValue[UpdatesResponse](json)
    if (response.ok) {
      response.result
    } else {
      logger.error(s"Message with bad status: [$response]")
      throw new Exception(s"Unable to parse bad message $json")
    }
  }

  def parseMessageResponse(json: String): MessageJson = {
    val response = mapper.readValue[MessageResponse](json)
    if (response.ok) {
      response.result
    } else {
      logger.error(s"Message with bad status: [$response]")
      throw new Exception(s"Unable to parse bad message $json")
    }
  }

  def parseMessage(json: String): MessageJson = {
    mapper.readValue[MessageJson](json)
  }

  def keyboardToJson(keyboard: Keyboard): String = {
    mapper.writeValueAsString(InlineKeyboardMarkup.fromKeyboard(keyboard))
  }

  private case class UpdatesResponse(ok: Boolean, result: List[UpdateJson])

  private case class MessageResponse(ok: Boolean, result: MessageJson)
}
