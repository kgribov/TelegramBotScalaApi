package com.kgribov.telegram.bot.state

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Files, Paths}

import scala.io.Source

class StateStore(filePath: String) {

  private val offsetFilePath = s"$filePath/offset"

  def loadState: (Long, BotState) = {
    (loadOffset, BotState())
  }

  def storeState(offset: Long, state: BotState): Long = {
    storeOffset(offset)
    offset
  }

  private def storeOffset(offset: Long): Unit = {
    val directory = new File(offsetFilePath).getParentFile
    if (!directory.exists()) {
      directory.mkdirs()
    }
    val writer = new BufferedWriter(new FileWriter(offsetFilePath))
    writer.write(offset.toString)
    writer.close()
  }

  private def loadOffset: Long = {
    if (Files.exists(Paths.get(offsetFilePath))) {
      val source = Source.fromFile(offsetFilePath)
      val content = source.getLines()
      if (content.hasNext) {
        content.next().toInt
      } else {
        0
      }
    } else {
      0
    }
  }
}
