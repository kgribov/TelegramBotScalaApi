package com.kgribov.telegram.scheduler

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}
import java.nio.file.{Files, Paths}
import java.time.ZonedDateTime

import cron4s._
import cron4s.lib.javatime._
import com.kgribov.telegram.model.{BOT_USER, Chat, Message}

import scala.collection.mutable
import scala.util.Random

class CommandsScheduler(schedulerStoreFile: Option[String] = None) {

  private val commandsCronStore = loadStore()

  def scheduleCommand(command: String, cron: String, chatId: Long, now: ZonedDateTime = ZonedDateTime.now()): Unit = {
    commandsCronStore.put(CommandScheduleKey(command, chatId), ScheduleCron(cron, now))
    syncStoreWithFile(commandsCronStore, schedulerStoreFile)
  }

  def removeCommandSchedule(command: String, chatId: Long): Unit = {
    commandsCronStore.remove(CommandScheduleKey(command, chatId))
    syncStoreWithFile(commandsCronStore, schedulerStoreFile)
  }

  def retrieveMessagesToSend(): List[Message] = {
    retrieveMessagesToSend(ZonedDateTime.now())
  }

  def retrieveMessagesToSend(now: ZonedDateTime): List[Message] = {
    val needToExecCommands = commandsCronStore.toList
      .filter(command => needToExec(command._2, now))

    needToExecCommands.foreach(commandToExec =>
      scheduleCommand(commandToExec._1.command, commandToExec._2.cron, commandToExec._1.chatId, now))

    needToExecCommands.map(commandToExec => Message(
      id = Random.nextString(10),
      from = BOT_USER,
      command = Some(commandToExec._1.command),
      replyTo = None,
      date = now,
      chat = Chat(commandToExec._1.chatId, None, None, "private"),
      text = ""
    ))
  }

  private def needToExec(scheduleCron: ScheduleCron, now: ZonedDateTime): Boolean = {
    val Right(cron) = Cron(scheduleCron.cron)
    val prevCronRun = cron.prev(now.toLocalDateTime)

    prevCronRun.exists(_.isAfter(scheduleCron.lastRun.toLocalDateTime))
  }

  private def loadStore(): mutable.HashMap[CommandScheduleKey, ScheduleCron] = {
    schedulerStoreFile.map(fileName => loadCommandsStore(fileName))
      .getOrElse(new mutable.HashMap[CommandScheduleKey, ScheduleCron]())
  }

  private def loadCommandsStore(fileName: String): mutable.HashMap[CommandScheduleKey, ScheduleCron] = {
    if (Files.exists(Paths.get(fileName))) {
      val ois = new ObjectInputStream(new FileInputStream(fileName))
      val store = ois.readObject.asInstanceOf[mutable.HashMap[CommandScheduleKey, ScheduleCron]]
      ois.close

      store
    } else {
      new mutable.HashMap[CommandScheduleKey, ScheduleCron]()
    }
  }

  private def syncStoreWithFile(store: mutable.HashMap[CommandScheduleKey, ScheduleCron], file: Option[String]): Unit = {
    file.foreach(fileName => {
      val oos = new ObjectOutputStream(new FileOutputStream(fileName))
      oos.writeObject(store)
      oos.close
    })
  }
}
