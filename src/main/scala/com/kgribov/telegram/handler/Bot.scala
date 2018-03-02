package com.kgribov.telegram.handler

import com.kgribov.telegram.model.Message
import com.kgribov.telegram.process.MessageProcessor
import com.kgribov.telegram.source.MessagesSource
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

class Bot(messageSource: MessagesSource,
          messageProcessor: MessageProcessor,
          loadPeriodSec: Int = 2) extends LazyLogging {

  def start(stopBot: => Boolean = false): Unit = {
    while (!stopBot) {
      val newMessages = loadNewMessages

      messageProcessor.processMessages(newMessages)

      Thread.sleep(loadPeriodSec * 1000)
    }
  }

  private def loadNewMessages: List[Message] = {
    val newMessages = Try(messageSource.getNewMessages())

    newMessages match {
      case Success(listOfMessages) => {
        logger.info(s"Received new batch of messages, batch size is [${listOfMessages.size}]")
        listOfMessages
      }
      case Failure(ex) => {
        logger.error("Unable to load new batch of messages", ex)
        List.empty[Message]
      }
    }
  }
}
