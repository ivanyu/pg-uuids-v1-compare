package me.ivanyu.pg_uuids_v1_compare

import java.util.UUID

import com.datastax.driver.core.utils.UUIDs

class UUIDV1Generator(unixTimestamp: Long) {
  import UUIDV1Generator._

  UUIDs.timeBased()

  private var generations = 0
  private var lastTimestamp = fromUnixTimestamp(unixTimestamp)

  def generate(): UUID = {
    generations += 1
    if (generations == 10001)
      throw new IllegalStateException("Not more than 10000 version 1 UUIDs per one UNIX timestamp.")

    new UUID(makeMSB(getCurrentTimestamp), CLOCK_SEQ_AND_NODE)
  }

  private def getCurrentTimestamp: Long = {
    val result = lastTimestamp
    lastTimestamp += 1
    result
  }
}

object UUIDV1Generator {
  private val CLOCK_SEQ_AND_NODE = {
    val field = classOf[UUIDs].getDeclaredField("CLOCK_SEQ_AND_NODE")
    field.setAccessible(true)
    field.getLong(null)
  }

  private def fromUnixTimestamp(tstamp: Long): Long = {
    val method = classOf[UUIDs].getDeclaredMethod("fromUnixTimestamp", classOf[Long])
    method.setAccessible(true)
    method.invoke(null, tstamp.asInstanceOf[AnyRef]).asInstanceOf[Long]
  }

  private def makeMSB(timestamp: Long): Long = {
    val method = classOf[UUIDs].getDeclaredMethod("makeMSB", classOf[Long])
    method.setAccessible(true)
    method.invoke(null, timestamp.asInstanceOf[AnyRef]).asInstanceOf[Long]
  }
}