package creed

import java.io.File

import org.mapdb._


package object query {

  type Token = String

  private[query] def makeDB(dbFile: File): DB =
    DBMaker.fileDB(dbFile)
           .asyncWriteFlushDelay(1)
           .cacheHardRefEnable
           .transactionDisable
           .closeOnJvmShutdown
           .compressionEnable
           .fileMmapEnableIfSupported
           .make

  private[query] def makeDB(dbPath: String): DB = makeDB(new File(dbPath))

}