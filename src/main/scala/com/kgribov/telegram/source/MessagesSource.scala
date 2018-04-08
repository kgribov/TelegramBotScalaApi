package com.kgribov.telegram.source

import com.kgribov.telegram.model.{Message, Update}
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

class MessagesSource(loadUpdates: Long => List[Update], offsetStore: OffsetStore) extends LazyLogging {

  def getNewMessages(): List[Message] = {
    val offset = offsetStore.loadOffset

    val tryUpdates = Try(loadUpdates(offset))
    tryUpdates match {
      case Success(updates) => {
        logger.info(s"Load ${updates.size} new updates")

        val nextOffset = if (updates.isEmpty) {
          offset
        } else {
          updates.map(_.id).max + 1
        }
        logger.info(s"Next offset is $nextOffset")
        offsetStore.store(nextOffset)

        val messages = updatesToMessages(updates)
        logger.info(s"Load ${messages.size} new messages")

        messages
      }
      case Failure(ex) => {
        val nextOffset = offset + 1
        logger.error(s"Unable to load new updates with offset $offset, next offset is $nextOffset", ex)
        offsetStore.store(nextOffset)
        List.empty[Message]
      }
    }
  }

  private def updatesToMessages(updates: List[Update]): List[Message] = {
    logger.info(s"Load ${updates.size} new messages")

    updates.flatMap(_.message)
  }
}
