package com.kgribov.telegram.source

class FileBasedOffsetStore(fileName: String) extends OffsetStore {

  override def loadOffset: Int = {
    0
  }

  override def store(offset: Int): Unit = {
    //nothing still
  }
}
