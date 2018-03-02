package com.kgribov.telegram.source

import java.time.ZonedDateTime

import com.kgribov.telegram.model.{Message, Update}
import org.scalatest.{FunSuite, Matchers}

class MessageSourceTest extends FunSuite with Matchers {

  test("message source should return 0 messages if no updates available") {
    val source = new MessagesSource(loadUpdates(Map()), new InMemoryOffsetStore)

    val messages = source.getNewMessages()

    messages should have size 0
  }

  test("message source should commit offset to offsetStore") {
    val offsetStore = new InMemoryOffsetStore
    val source = new MessagesSource(
      loadUpdates(
        Map(
          0 -> List(update(10), update(0))
        )),
      offsetStore)

    val messages = source.getNewMessages()

    offsetStore.getCurrentOffset should be (11)
  }

  test("message source should load data by provided offset") {
    val offsetStore = new InMemoryOffsetStore(100)
    val source = new MessagesSource(
      loadUpdates(
        Map(
          0 -> List(update(10), update(0)),
          100 -> List(update(100))
        )),
      offsetStore)

    val messages = source.getNewMessages()

    messages.map(_.id).max should be (100)
  }

  private def loadUpdates(returnResults: Map[Int, List[Update]]): (Int) => List[Update] = {
    (offset: Int) => returnResults.getOrElse(offset, List.empty[Update])
  }

  private def update(id: Int): Update = {
    Update(id, Message(id.toString, null, None, None, false, ZonedDateTime.now(), null, "hello"))
  }
}
