package com.kgribov.telegram.process

import java.time.{Clock, ZoneId, ZonedDateTime}

import com.kgribov.telegram.model.Message
import com.kgribov.telegram.sender.MessageSender
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

class DialogPool(messageSender: MessageSender,
                 clock: Clock = Clock.systemDefaultZone()) extends LazyLogging {

  private val activeDialogs = new mutable.HashMap[Long, DialogProcessor]()

  def addDialog(chatId: Long, dialog: DialogProcessor): Unit = {
    activeDialogs.put(chatId, dialog)
  }

  def processMessages(messages: List[Message]): Unit = {
    cleanDialogs()

    logger.info(s"Current number of active dialogs is ${activeDialogs.size}")

    val messagesToChat = messages.groupBy(_.chat.id)
    activeDialogs.keySet.foreach(key => {
      val messagesForDialog = messagesToChat.get(key)
      val dialogProcessor = activeDialogs(key)
      processMessagesSafely(dialogProcessor, messagesForDialog)
    })
  }

  private def processMessagesSafely(dialogProcessor: DialogProcessor, messages: Option[List[Message]]): Unit = {
    val processResult = Try(dialogProcessor.processMessages(messages))
    processResult match {
      case Success(_) => logger.debug(s"Successfully process dialog messages: $messages")
      case Failure(ex) => logger.error(s"Unable to process messages for dialog : $messages", ex)
    }
  }

  private def cleanDialogs(): Unit = {
    val activeChats = activeDialogs.keySet
    activeChats
      .filter(chatId => !activeDialogs(chatId).isActive(now))
      .foreach(activeDialogs.remove)
  }

  private def now: ZonedDateTime = clock.instant().atZone(ZoneId.systemDefault())
}
