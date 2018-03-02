package com.kgribov.telegram.source

trait OffsetStore {

  def loadOffset: Int

  def store(offset: Int)
}
