package me.ivanyu.pg_uuids_v1_compare

import java.util.UUID

import com.datastax.driver.core.utils.UUIDs
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, FlatSpec}
import scalikejdbc.{ConnectionPool, GlobalSettings}

import scala.util.Random

class CompareTest extends FlatSpec with Matchers {

  import scalikejdbc._

  // Init DB
  def init(): Unit = {
    val config = ConfigFactory.load()
    val address = config.getString("db.address")
    val port = config.getInt("db.port")
    val dbName = config.getString("db.database")
    val user = config.getString("db.user")
    val password = config.getString("db.password")
    val url = s"jdbc:postgresql://$address:$port/$dbName"

    val settings = ConnectionPoolSettings().copy(
      connectionTimeoutMillis = 25000L,
      maxSize = 16
    )
    ConnectionPool.singleton(url, user, password, settings)
  }
  init()

  it should "correctly work with equal UUIDs" in {
    val generator1 = new UUIDV1Generator(0)
    val generator2 = new UUIDV1Generator(System.currentTimeMillis())
    val generator3 = new UUIDV1Generator(Int.MaxValue)

    val pairs = (0 to 9999).flatMap { _ =>
      val u1 = generator1.generate()
      val u2 = generator2.generate()
      val u3 = generator3.generate()
      Seq((u1, u1), (u2, u2), (u3, u3))
    }
    compareUUIDPairs(pairs:_*)
  }

  it should "correctly compare UUIDs within one timestamp" in {
    val generators = List(
      new UUIDV1Generator(0),
      new UUIDV1Generator(System.currentTimeMillis()),
      new UUIDV1Generator(Int.MaxValue)
    )
    generators.foreach { generator =>
      val uuids = (0 to 9999).map(_ => generator.generate()).toVector

      // Sequentially
      compareUUIDPairs(seqToPairs(uuids):_*)
      compareUUIDPairs(seqToPairs(uuids.reverse):_*)

      // Randomly
      val randomPairs = (0 to 50000).map { _ =>
        val i = Random.nextInt(10000)
        val j = Random.nextInt(10000)
        (uuids(i), uuids(j))
      }
      compareUUIDPairs(randomPairs:_*)
    }
  }

  it should "correctly work with UUIDs with different timestamps" in {
    val span = 500
    val starts = List(
      0, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000, Int.MaxValue - span)
    starts.foreach { start =>
      val generators = (0 to span).map(d => new UUIDV1Generator(start + d)).toVector
      val uuids = (0 to (span * 100)).map { _ =>
        val gen = generators(Random.nextInt(span))
        gen.generate()
      }

      // Sequentially
      compareUUIDPairs(seqToPairs(uuids):_*)
      compareUUIDPairs(seqToPairs(uuids.reverse):_*)

      // Randomly
      val randomPairs = seqToRandomPairs(uuids, 10000)
      compareUUIDPairs(randomPairs:_*)
    }
  }

  private def compareUUIDPairs(pairs: (UUID, UUID)*): Unit = {
    val selectVals = pairs.map {
      case (u1, u2) => s"uuids_v1_compare(uuid('$u1'), uuid('$u2'))"
    }.mkString(",")

    val query = s"SELECT ARRAY[$selectVals];"

    val result = DB.readOnly { session =>
      session.single(query) { rs =>
        val a = rs.array(1).getArray
        a.asInstanceOf[Array[java.lang.Integer]]
          .map(Integer2int)
          .toList
      }.get
    }
    assert(result.length == pairs.length)

    (pairs zip result).foreach {
      case ((u1, u2), dbResult) =>
        val javaResult = u1.timestamp().compare(u2.timestamp())

        if (dbResult != javaResult) {
          println(u1)
          println(u2)
          println(dbResult)
          println(javaResult)
        }
        dbResult shouldBe javaResult
    }
  }

  private def seqToPairs[T](seq: Seq[T]): List[(T, T)] = {
    seq.sliding(2).map(x => (x.head, x(1))).toList
  }

  private def seqToRandomPairs[T](seq: Seq[T], count: Int): List[(T, T)] = {
    val randomPairs = (0 to count).map { _ =>
      val i = Random.nextInt(seq.length)
      val j = Random.nextInt(seq.length)
      (seq(i), seq(j))
    }
    randomPairs.toList
  }
}

// План тестирования
// 3. Разные таймстэмпы, разные номера
// 4. Последовательно идущией таймстэмпы
