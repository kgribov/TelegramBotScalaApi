package com.kgribov.telegram.source

import com.kgribov.telegram.model.{Message, Update}
import com.typesafe.scalalogging.LazyLogging

class MessagesSource(loadUpdates: Int => List[Update], offsetStore: OffsetStore) extends LazyLogging {

  def getNewMessages(): List[Message] = {
    val offset = offsetStore.loadOffset
    val updates = loadUpdates(offset)

    logger.info(s"Load [${updates.size}] new messages")
    logger.debug(s"Load next messages: $updates")

    val nextOffset = if (updates.isEmpty) {
      offset
    } else {
      updates.map(_.id).max + 1
    }

    logger.info(s"Next offset is [$nextOffset]")

    offsetStore.store(nextOffset)

    updates.filter(_.message != null).map(_.message)
  }
}
