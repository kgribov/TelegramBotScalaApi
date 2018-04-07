package com.kgribov.telegram.source

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Files, Paths}

import scala.io.Source

trait OffsetStore {

  def loadOffset: Long

  def store(offset: Long)
}

class InMemoryOffsetStore(startOffset: Long = 0) extends OffsetStore  {

  private var currentOffset: Long = startOffset

  override def loadOffset: Long = currentOffset

  override def store(offset: Long): Unit = {
    currentOffset = offset
  }

  def getCurrentOffset: Long = currentOffset
}

class FileBasedOffsetStore(botName: String, storePath: String = s"${System.getProperty("user.home")}/telegram_bots") extends OffsetStore {

  private val offsetFilePath = s"$storePath/$botName/offset"

  private var currentOffset: Option[Long] = None

  override def loadOffset: Long = {
    if (currentOffset.isEmpty) {
      val loadedOffset = loadFromFile
      currentOffset = Some(loadedOffset)
    }
    currentOffset.get
  }

  override def store(offset: Long): Unit = {
    currentOffset = Some(offset)
    storeToFile(offset)
  }

  private def storeToFile(offset: Long): Unit = {
    val directory = new File(offsetFilePath).getParentFile
    if (!directory.exists()) {
      directory.mkdirs()
    }
    val writer = new BufferedWriter(new FileWriter(offsetFilePath))
    writer.write(offset.toString)
    writer.close()
  }

  private def loadFromFile: Int = {
    if (Files.exists(Paths.get(offsetFilePath))) {
      val source = Source.fromFile(offsetFilePath)
      val content = source.getLines().next()
      if (content.isEmpty) {
        0
      } else {
        content.toInt
      }
    } else {
      0
    }
  }
}
