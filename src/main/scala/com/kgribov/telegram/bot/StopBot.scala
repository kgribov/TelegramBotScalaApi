package com.kgribov.telegram.bot

import java.util.concurrent.atomic.AtomicBoolean

trait StopBot extends (() => Boolean) {

}

class NeverStopBot extends StopBot {
  override def apply() = false
}

class StopBotOnSignal extends StopBot {
  private val stop = new AtomicBoolean(false)
  sys.addShutdownHook(stop.set(true))

  override def apply() = {
    stop.get()
  }
}
