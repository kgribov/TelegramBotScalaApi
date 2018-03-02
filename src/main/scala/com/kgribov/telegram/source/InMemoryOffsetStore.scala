package com.kgribov.telegram.source

class InMemoryOffsetStore(startOffset: Int = 0) extends OffsetStore  {

  private var currentOffset = startOffset

  override def loadOffset: Int = currentOffset

  override def store(offset: Int): Unit = {
    currentOffset = offset
  }

  def getCurrentOffset: Int = currentOffset
}
