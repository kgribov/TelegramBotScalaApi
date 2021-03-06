package com.kgribov.telegram.bot

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.kgribov.telegram.bot.json._
import com.kgribov.telegram.bot.model._
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

  def parseAdminsResponse(json: String): List[ChatMemberJson] = {
    val response = mapper.readValue[AdministratorsResponse](json)
    if (response.ok) {
      response.result
    } else {
      List()
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

  private case class AdministratorsResponse(ok: Boolean, result: List[ChatMemberJson])
}
