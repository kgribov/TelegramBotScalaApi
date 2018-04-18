package com.kgribov.telegram.source

import java.time.ZonedDateTime

import com.kgribov.telegram.model.{Message, Update}
import org.scalatest.{FunSuite, Matchers}

class MessageSourceTest extends FunSuite with Matchers {

  test("message source should return 0 messages if no updates available") {
    val source = new MessagesSource(loadUpdates(Map()), new InMemoryOffsetStore)

    val messages = source.loadNewMessages()

    messages should have size 0
  }

  test("message source should commit new offset to offsetStore") {
    val offsetStore = new InMemoryOffsetStore
    val source = new MessagesSource(
      loadUpdates(
        Map(
          0L -> List(update(10), update(0))
        )),
      offsetStore)

    val messages = source.loadNewMessages()

    offsetStore.getCurrentOffset should be (11)
  }

  test("message source should load data by provided offset") {
    val offsetStore = new InMemoryOffsetStore(100)
    val source = new MessagesSource(
      loadUpdates(
        Map(
          0L -> List(update(10), update(0)),
          100L -> List(update(100))
        )),
      offsetStore)

    val messages = source.loadNewMessages()

    messages.map(_.id).max should be ("100")
    offsetStore.getCurrentOffset should be (101)
  }

  test("message source should take next offset if load was failed") {
    val offsetStore = new InMemoryOffsetStore
    val source = new MessagesSource(_ => throw new RuntimeException("Unable to load"), offsetStore)

    val messages = source.loadNewMessages()

    messages.size should be (0)
    offsetStore.getCurrentOffset should be (1)
  }

  private def loadUpdates(returnResults: Map[Long, List[Update]]): (Long) => List[Update] = {
    (offset: Long) => returnResults.getOrElse(offset, List.empty[Update])
  }

  private def update(id: Int): Update = {
    Update(id, Some(Message(id.toString, null, None, None, false, ZonedDateTime.now(), null, "hello")))
  }
}
