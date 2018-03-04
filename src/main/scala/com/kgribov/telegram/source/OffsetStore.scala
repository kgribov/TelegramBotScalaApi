package com.kgribov.telegram.source

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Files, Paths}

import scala.io.Source

trait OffsetStore {

  def loadOffset: Int

  def store(offset: Int)
}

class InMemoryOffsetStore(startOffset: Int = 0) extends OffsetStore  {

  private var currentOffset = startOffset

  override def loadOffset: Int = currentOffset

  override def store(offset: Int): Unit = {
    currentOffset = offset
  }

  def getCurrentOffset: Int = currentOffset
}

class FileBasedOffsetStore(botName: String, storePath: String = s"${System.getProperty("user.home")}/telegram_bots") extends OffsetStore {

  private val offsetFilePath = s"$storePath/$botName/offset"

  private var currentOffset: Option[Int] = None

  override def loadOffset: Int = {
    if (currentOffset.isEmpty) {
      val loadedOffset = loadFromFile
      currentOffset = Some(loadedOffset)
    }
    currentOffset.get
  }

  override def store(offset: Int): Unit = {
    currentOffset = Some(offset)
    storeToFile(offset)
  }

  private def storeToFile(offset: Int): Unit = {
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
      val content = source.mkString
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
